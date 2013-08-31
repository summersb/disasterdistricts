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

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.MouseEvent;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.resize.ResizeMapEvent;
import com.google.gwt.maps.client.events.resize.ResizeMapHandler;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import org.lds.disasterlocator.client.load.LoadPlace;
import org.lds.disasterlocator.shared.Member;

/**
 *
 * @author Bert W Summers
 */
public class MapViewImpl extends Composite implements MapView {

    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);
    @UiField HTMLPanel map;
    @UiField HTMLPanel topMenu;
    @UiField Button load;
    @UiField Button compute;

    private Activity activity;
    private List<Member> memberList;

    public MapViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void renderMap() {
        MapOptions options = MapOptions.newInstance();
        options.setZoom(12);
        options.setMapTypeId(MapTypeId.ROADMAP);

        mapWidget = new MapWidget(options);
        map.clear();
        map.add(mapWidget);

        mapWidget.addResizeHandler(new ResizeMapHandler() {
            @Override
            public void onEvent(ResizeMapEvent event) {
                mapWidget.setSize(Window.getClientWidth() + "px", Window.getClientHeight() + "px");
            }
        });

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

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    interface MapUiBinder extends UiBinder<Widget, MapViewImpl> {
    }
    private MapWidget mapWidget;

    private void draw() {

        map.clear();

        renderMap();
//        createSpiderdfier();
    }

    private String memberInfo(Member member) {
        String rollover;
        String[] tokens;
        rollover = member.getHousehold();
        rollover += "\n";
        tokens = member.getAddress().split("[,]");
        rollover += tokens[0];
        rollover += "\n";
        rollover += "District: " + member.getDistrict();
        return rollover;
    }

    protected void drawInfoWindow(Marker marker, MouseEvent mouseEvent, Member member) {
        if (marker == null || mouseEvent == null) {
            return;
        }
        VerticalPanel panel = new VerticalPanel();

        HTML html = new HTML("<b>" + member.getHousehold() + "</b>");
        panel.add(html);
        String[] tokens = member.getAddress().split("[,]");
        Label lbl = new Label(tokens[0]);
        panel.add(lbl);
        CheckBox checkbox  = new CheckBox("District Leader", true);
        panel.add(checkbox);
        checkbox  = new CheckBox("Automatic Assignment", true);
        panel.add(checkbox);
        lbl = new Label("District: " + member.getDistrict());
        panel.add(lbl);
        TextBox tb = new TextBox();
         panel.add(tb);

        InfoWindowOptions options = InfoWindowOptions.newInstance();
        options.setContent(panel);

        InfoWindow iw = InfoWindow.newInstance(options);

        iw.open(mapWidget, marker);
    }

    private String rollOver(Member member) {
        String rollover;
        String[] tokens;
        rollover = member.getHousehold();
        rollover += "\n";
        tokens = member.getAddress().split("[,]");
        rollover += tokens[0];
        rollover += "\n";
        rollover += "District: " + member.getDistrict();
        return rollover;
    }

    private class MarkerHandler implements ClickMapHandler {

        Marker marker;
        Member member;

        MarkerHandler(Marker marker, Member member) {
            this.marker = marker;
            this.member = member;
        }

        @Override
        public void onEvent(ClickMapEvent event) {
            drawInfoWindow(marker, event.getMouseEvent(), member);
        }
    }

    @Override
    public void plotHouses(List<Member> members) {
        memberList = members;
        for (final Member member : members) {
            LatLng center = LatLng.newInstance(Double.parseDouble(member.getLat()), Double.parseDouble(member.getLng()));
            MarkerOptions options = MarkerOptions.newInstance();
            options.setPosition(center);
            options.setTitle(rollOver(member));

            final Marker marker = Marker.newInstance(options);
            marker.setMap(mapWidget);

            marker.addClickHandler(new MarkerHandler(marker, member));

        }
    }

    @UiHandler("load")
    public void loadData(ClickEvent event) {
        activity.goTo(new LoadPlace("loaddata"));
    }

    @UiHandler("compute")
    public void computeMembers(ClickEvent event){
        ComputeDistrictMembers computeDistrictMembers = new ComputeDistrictMembers(activity.getClientFactory());
        computeDistrictMembers.compute(memberList);
    }

    private native void createSpiderdfier()/*-{
     oms = new OverlappingMarkerSpiderfier(map, {
     markersWontMove: true,
     markersWontHide: true}
     );
     }-*/;
}
