/**
 * Copyright (C) 2013
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
package org.lds.disasterlocator.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.MouseEvent;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MemberList;

/**
 *
 * @author Bert W Summers
 */
public class Map extends Composite {

    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);

    @UiField
    HTMLPanel map;
    private ClientFactory clientFactory;

    public Map(){
        initWidget(uiBinder.createAndBindUi(this));
        draw();
    }

    public void setClientFactory(ClientFactory factory){
        this.clientFactory = factory;
    }

    private void renderMap() {
        MapOptions options = MapOptions.newInstance();
//        options.setCenter(center);
        options.setZoom(12);
        options.setMapTypeId(MapTypeId.ROADMAP);

        mapWidget = new MapWidget(options);
        map.clear();
        map.add(mapWidget);

        mapWidget.setSize(Window.getClientWidth() + "px", Window.getClientHeight() + "px");

        Geolocation geolocation = Geolocation.getIfSupported();
        geolocation.getCurrentPosition(new Callback<Position, PositionError>() {
            @Override
            public void onSuccess(Position result) {
                centerOn(result.getCoordinates());
            }

            @Override
            public void onFailure(PositionError reason) {
                Window.alert(reason.getMessage());
            }
        });

    }

    private void centerOn(Position.Coordinates coordinates) {
        double latitude = coordinates.getLatitude();
        double longitude = coordinates.getLongitude();
        LatLng center = LatLng.newInstance(latitude, longitude);
        mapWidget.setCenter(center);

    }

    interface MapUiBinder extends UiBinder<Widget, Map> {
    }
    private MapWidget mapWidget;

    private void draw() {

        map.clear();

        renderMap();
//        createSpiderdfier();
        plotHouses();
    }

    protected void drawInfoWindow(Marker marker, MouseEvent mouseEvent) {
        if (marker == null || mouseEvent == null) {
            return;
        }

        HTML html = new HTML("You clicked on: " + mouseEvent.getLatLng().getToString());

        InfoWindowOptions options = InfoWindowOptions.newInstance();
        options.setContent(html);
        InfoWindow iw = InfoWindow.newInstance(options);
        iw.open(mapWidget, marker);
    }

    private void drawMap() {
        LatLng center = LatLng.newInstance(49.496675, -102.65625);
        MapOptions opts = MapOptions.newInstance();
        opts.setZoom(4);
        opts.setCenter(center);
        opts.setMapTypeId(MapTypeId.HYBRID);

        mapWidget = new MapWidget(opts);
        map.add(mapWidget);
        mapWidget.setSize("750px", "500px");

        mapWidget.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                // TODO fix the event getting, getting ....
                GWT.log("clicked on latlng=" + event.getMouseEvent().getLatLng());
            }
        });
    }

    private void plotHouses() {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "rest/member/list");
        rb.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            rb.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {

                    String json = response.getText();
                    AutoBean<MemberList> memberListAB = AutoBeanCodex.decode(clientFactory.getAutoBeanFactory(), MemberList.class, "{\"members\":" + json + "}");
                    MemberList memberList = memberListAB.as();
                    for (Member member : memberList.getMembers()) {
                        LatLng center = LatLng.newInstance(Double.parseDouble(member.getLat()), Double.parseDouble(member.getLng()));
                        MarkerOptions options = MarkerOptions.newInstance();
                        options.setPosition(center);
                        options.setTitle(member.getHousehold());

                        final Marker markerBasic = Marker.newInstance(options);
                        markerBasic.setMap(mapWidget);

                        markerBasic.addClickHandler(new ClickMapHandler() {
                            @Override
                            public void onEvent(ClickMapEvent event) {
                                drawInfoWindow(markerBasic, event.getMouseEvent());
                            }
                        });

                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error occured " + exception.getLocalizedMessage());
                }
            });
        } catch (RequestException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private native void createSpiderdfier()/*-{
        oms = new OverlappingMarkerSpiderfier(map, {
        markersWontMove: true,
        markersWontHide: true}
        );
        }-*/;
}