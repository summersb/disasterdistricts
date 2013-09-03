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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.server.rest.jpa.DistanceJpa;
import org.lds.disasterlocator.server.rest.json.DistanceMatrixResponse;
import org.lds.disasterlocator.server.rest.json.DistrictMatrixRequest;
import org.lds.disasterlocator.server.rest.json.Element;
import org.lds.disasterlocator.server.rest.json.LatLng;
import org.lds.disasterlocator.server.rest.json.Row;

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
     * @param fromlat
     * @param fromlng
     * @param tolat
     * @param tolng
     * @return
     */
    @GET
    @Path("/{fromlat}/{fromlng}/{tolat}/{tolng}")
    public Response getDistance(@PathParam("fromlat") double fromlat,
        @PathParam("fromlat") double fromlng,
        @PathParam("fromlat") double tolat,
        @PathParam("fromlat") double tolng){
        EntityManager em = emf.createEntityManager();
        TypedQuery<DistanceJpa> query = em.createNamedQuery("Distance.find", DistanceJpa.class);
        query.setParameter("fromLat", fromlat);
        query.setParameter("fromLng", fromlng);
        query.setParameter("toLat", tolat);
        query.setParameter("toLng", tolng);
        List<DistanceJpa> resultList = query.getResultList();
        if(resultList.isEmpty()){
            return Response.status(Status.NOT_FOUND).build();
        }
        DistanceJpa distance = resultList.get(0);
        em.close();
        return Response.ok().entity(new Integer(distance.getDistance())).build();
    }

    @POST
    public Response getDistanceMatrix(DistrictMatrixRequest dmr){
        StringBuilder sb = new StringBuilder("http://maps.googleapis.com/maps/api/distancematrix/json?");
        // add origin
        double fromLat = dmr.getOrigins().get(0).getJb();
        double fromLng = dmr.getOrigins().get(0).getKb();
        sb.append("origins=").append(fromLat).append(",").append(fromLng);
        // add destinations
        sb.append("&destinations=");
        // TODO loop here and send request for every 10 address
        for (LatLng latLng : dmr.getDestinations()) {
            double toLat = latLng.getJb();
            double toLng = latLng.getKb();
            // check if we already have destination
            Response distance = getDistance(fromLat, fromLng, toLat, toLng);
            if(distance.getStatus() == 404){
                sb.append(toLat).append(",").append(toLng);
                sb.append("|");
            }
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("&sensor=false&mode=walking&units=metric");
        logger.info(sb.toString());
        try {
            // request url
            URL url = new URL(sb.toString());
            try(InputStream is = url.openStream()){
                // convert back to DistanceMatrixResponse
                ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                DistanceMatrixResponse response = mapper.readValue(is, DistanceMatrixResponse.class);
                if(response.getStatus().equals("OK")){
                    List<Row> rows = response.getRows();
                    Row row = rows.get(0);
                    List<Element> elements = row.getElements();
                    List<LatLng> destinations = dmr.getDestinations();
                    for (int i = 0; i < elements.size(); i++) {
                        LatLng latLng = destinations.get(i);
                        // save address in db
                        Element element = elements.get(i);
                        saveDistance(fromLat, fromLng, latLng.getJb(), latLng.getKb(), element.getDistance().getValue());
                    }
                }else{
        // note only request 10 at a time
        // check status to see if delay required
        // add address to db
                    logger.severe("Received bad response " + response.getStatus());
                    return Response.serverError().build();
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Bad URL for distance matrix", ex);
        }
        return Response.ok().build();
    }

    private void saveDistance(double fromLat, double fromLng, double toLat, double toLng, int value) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        DistanceJpa dist = new DistanceJpa();
        dist.setFromLng(fromLng);
        dist.setFromLat(fromLat);
        dist.setToLat(toLat);
        dist.setToLng(toLng);
        dist.setDistance(value);
        em.persist(dist);
        em.getTransaction().commit();
    }
}
