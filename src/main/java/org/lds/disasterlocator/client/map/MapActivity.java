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
package org.lds.disasterlocator.client.map;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.ClientFactory;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MemberList;

/**
 *
 * @author Bert W Summers
 */
public class MapActivity extends AbstractActivity implements MapView.Activity {

    private final ClientFactory clientFactory;
    private final MapView view;

    public MapActivity(ClientFactory factory) {
        clientFactory = factory;
        view = clientFactory.getMapView();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setActivity(this);
        panel.setWidget((IsWidget) view);
        view.renderMap();
        loadMemberData();
        loadDistrictData();
    }

    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public AutoBeanFactory getAutoBeanFactory() {
        return clientFactory.getAutoBeanFactory();
    }

    private void loadMemberData() {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "rest/member/list");
        rb.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            rb.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {

                    String json = response.getText();
                    AutoBeanFactory autoBeanFactory = clientFactory.getAutoBeanFactory();
                    AutoBean<MemberList> memberListAB = AutoBeanCodex.decode(autoBeanFactory, MemberList.class, "{\"members\":" + json + "}");
                    MemberList memberList = memberListAB.as();
                    view.plotHouses(memberList.getMembers());
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error occured " + exception.getLocalizedMessage());
                }
            });
        } catch (RequestException ex) {
            Logger.getLogger(MapViewImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadDistrictData() {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "rest/district/list");
        rb.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            rb.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {

                    String json = response.getText();
                    AutoBeanFactory autoBeanFactory = clientFactory.getAutoBeanFactory();
                    AutoBean<DistrictList> memberListAB = AutoBeanCodex.decode(autoBeanFactory, DistrictList.class, "{\"districts\":" + json + "}");
                    DistrictList districtlist = districtListAB.as();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error occured " + exception.getLocalizedMessage());
                }
            });
        } catch (RequestException ex) {
            Logger.getLogger(MapViewImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
