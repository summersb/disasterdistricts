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
package org.lds.disasterlocator.client.map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.geometrylib.SphericalUtils;
import com.google.gwt.maps.client.overlays.Circle;
import com.google.gwt.maps.client.overlays.CircleOptions;
import com.google.gwt.maps.client.services.DistanceMatrixRequest;
import com.google.gwt.maps.client.services.DistanceMatrixRequestHandler;
import com.google.gwt.maps.client.services.DistanceMatrixResponse;
import com.google.gwt.maps.client.services.DistanceMatrixResponseElement;
import com.google.gwt.maps.client.services.DistanceMatrixResponseRow;
import com.google.gwt.maps.client.services.DistanceMatrixService;
import com.google.gwt.maps.client.services.DistanceMatrixStatus;
import com.google.gwt.maps.client.services.Geocoder;
import com.google.gwt.maps.client.services.TravelMode;
import com.google.gwt.maps.client.services.UnitSystem;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.ClientFactory;
import org.lds.disasterlocator.shared.District;
import org.lds.disasterlocator.shared.DistrictList;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MyConstants;

/**
 *
 * @author Bert Summers
 */
public class ComputeDistrictMembers {
    private final ClientFactory clientFactory;
    private List<Member> memberList;
    private static final Logger logger = Logger.getLogger(ComputeDistrictMembers.class.getName());
    private final MapWidget map;

    public ComputeDistrictMembers(ClientFactory factory, MapWidget map) {
        this.clientFactory = factory;
        this.map = map;
    }

    public void compute(List<Member> memberList) {
        this.memberList = memberList;
        // get district leaders
        loadDistrictLeaders();

    }

    private void assignToDistrict(DistrictList list){
        // get members within 1,000 meters
        for (District district : list.getDistricts()) {
            Member leader = district.getLeader();
            String lat = leader.getLat();
            String lng = leader.getLng();
            DistanceMatrixRequest dmr = DistanceMatrixRequest.newInstance();
            LatLng leaderLatLng = LatLng.newInstance(Double.parseDouble(lat), Double.parseDouble(lng));
            JsArray array = (JsArray) JsArray.createArray();
            array.push(leaderLatLng);
            dmr.setOrigins(array);
            JsArray memberArray = (JsArray) JsArray.createArray();
            for (Member member : memberList) {
                String memberlat = member.getLat();
                String memberlng = member.getLng();
                LatLng memberlatlng = LatLng.newInstance(Double.parseDouble(memberlat), Double.parseDouble(memberlng));
                double distance = SphericalUtils.computeDistanceBetween(leaderLatLng, memberlatlng);
                if(distance < 1000){
                    if(!member.getHousehold().equals(leader.getHousehold())){
                        memberArray.push(memberlatlng);
                    }
                }
            }
            dmr.setDestinations(memberArray);
            dmr.setTravelMode(TravelMode.WALKING);
            dmr.setUnitSystem(UnitSystem.METRIC);
            RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, MyConstants.REST_URL + "distance");
            rb.setHeader(MyConstants.CONTENT_TYPE, MyConstants.APPLICATION_JSON);
            String json = new JSONObject(dmr).toString();
            logger.info(json);
            try {
                rb.sendRequest(json, new RequestCallback() {

                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        // completed call
                        // TODO
                        // update map view, remove all markers and re-add
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });
            } catch (RequestException ex) {
                Logger.getLogger(ComputeDistrictMembers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadDistrictLeaders(){
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "rest/district/list");
        rb.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            rb.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {

                    String json = response.getText();
                    AutoBeanFactory autoBeanFactory = clientFactory.getAutoBeanFactory();
                    AutoBean<DistrictList> districtListAB = AutoBeanCodex.decode(autoBeanFactory, DistrictList.class, "{\"districts\":" + json + "}");
                    DistrictList districtList = districtListAB.as();
                    assignToDistrict(districtList);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error occured " + exception.getLocalizedMessage());
                }
            });
        } catch (RequestException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

}