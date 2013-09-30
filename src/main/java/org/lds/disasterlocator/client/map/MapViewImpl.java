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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.events.MouseEvent;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.resize.ResizeMapEvent;
import com.google.gwt.maps.client.events.resize.ResizeMapHandler;
import com.google.gwt.maps.client.overlays.Circle;
import com.google.gwt.maps.client.overlays.CircleOptions;
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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.load.LoadPlace;
import org.lds.disasterlocator.client.district.DistrictPlace;
import org.lds.disasterlocator.shared.District;
import org.lds.disasterlocator.shared.Member;

/**
 *
 * @author Bert W Summers
 */
public class MapViewImpl extends Composite implements MapView {

    private static final Logger logger = Logger.getLogger(MapViewImpl.class.getName());
    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);
    @UiField
    HTMLPanel map;
    @UiField
    HTMLPanel topMenu;
    @UiField
    Button load;
    @UiField
    Button compute;
    @UiField
    CheckBox districtCircle;
    @UiField
    Button reports;
    private Activity activity;
    private List<Member> memberList;
    private List<District> districtList;
    private Map<String, Marker> markerSet = new HashMap<String, Marker>();
    private static final String LEADER_COLOR = "|00FF00|000000";
    private static final String MEMBER_COLOR = "|FF0000|000000";
    private List<Circle> districtCircleList = new ArrayList<Circle>();

    public MapViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void renderMap() {
        if(mapWidget == null){
        MapOptions options = MapOptions.newInstance();
            options.setZoom(13);
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
        }
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

    @Override
    public void clearState() {
        memberList = null;
        districtList = null;
        for (Circle circle : districtCircleList) {
            circle.setMap(null);
        }
        districtCircleList.clear();
        Set<String> keySet = markerSet.keySet();
        for (String key : keySet) {
            Marker m = markerSet.get(key);
            m.setMap((MapWidget) null);
        }
        markerSet.clear();
    }

    @Override
    public void setMembers(List<Member> members) {
        memberList = members;
        plotHouses();
    }

    interface MapUiBinder extends UiBinder<Widget, MapViewImpl> {
    }
    private MapWidget mapWidget;

    protected void drawInfoWindow(final Marker marker, MouseEvent mouseEvent, final Member memberIn) {

        if (marker == null || mouseEvent == null) {
            return;
        }
        VerticalPanel vert = new VerticalPanel();

        for (final Member member : getMembersAtLocation(memberIn)) {
            HTML html = new HTML("<b>" + member.getHousehold() + "</b>");
            vert.add(html);
            String[] tokens = member.getAddress().split("[,]");
            Label lbl = new Label(tokens[0]);
            vert.add(lbl);
            CheckBox checkbox = new CheckBox("District Leader", true);
            checkbox.setValue(activity.isLeader(member));


            // Hook up a handler to find out when it's clicked.
            checkbox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    boolean checked = ((CheckBox) event.getSource()).getValue();
                    if (checked) {
                        activity.setLeader(member);
                    } else {
                        activity.deleteLeader(member);
                    }
                }
            });

            vert.add(checkbox);
            checkbox = new CheckBox("Automatic Assignment", true);
            checkbox.setValue(member.getAuto());

            // Hook up a handler to find out when it's clicked.
            checkbox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    boolean checked = ((CheckBox) event.getSource()).getValue();
                    member.setAuto(checked);
                    activity.setAuto(member);
                }
            });

            vert.add(checkbox);
            HorizontalPanel horiz = new HorizontalPanel();
            lbl = new Label("District:");
            horiz.add(lbl);
            final ListBox lb = new ListBox(false);
            for (District district : districtList) {
                lb.addItem(district.getLeader().getHousehold(), Integer.toString(district.getId()));
            }
            // find the right district to mark as selected
            for (int i = 0; i < lb.getItemCount(); i++) {
                if (lb.getValue(i).equals(Integer.toString(member.getDistrict()))) {
                    lb.setSelectedIndex(i);
                }
            }
            lb.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    int id = Integer.parseInt(lb.getValue(lb.getSelectedIndex()));
                    member.setDistrict(id);
                    activity.updateMember(member);
                    marker.setIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=" + id + MEMBER_COLOR);
                    marker.setTitle(Integer.toString(id));
                }
            });

            horiz.add(lb);
            vert.add(horiz);
        }
        InfoWindowOptions options = InfoWindowOptions.newInstance();
        options.setContent(vert);

        InfoWindow iw = InfoWindow.newInstance(options);

        iw.open(mapWidget, marker);
    }

    private String getDistrict(Member member) {
        int district;
        String result;
        district = member.getDistrict();
        if (district < 0) {
            result = "unassigned";
        } else {
            result = String.valueOf(member.getDistrict());
        }
        return result;
    }

    private String rollOver(Member member) {
        String rollover;
        String[] tokens;
        rollover = member.getHousehold();
        rollover += "\n";
        tokens = member.getAddress().split("[,]");
        rollover += tokens[0];
        rollover += "\n";
        rollover += "District: " + getDistrict(member);
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
    public void setDistricts(List<District> list) {
        districtList = list;
        plotHouses();
    }

    private boolean isLeader(String household) {
        if (districtList != null) {
            for (District district : districtList) {
                if (district.getLeader().getHousehold().equals(household)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void plotHouses() {
        if (memberList != null && districtList != null) {
            for (final Member member : memberList) {
                LatLng center = LatLng.newInstance(member.getLat(), member.getLng());
                MarkerOptions options = MarkerOptions.newInstance();
                options.setPosition(center);
                options.setTitle(rollOver(member));
                if (member.getDistrict() != 0) {
                    // we can set district colors here also
                    String color = member.getDistrict() + MEMBER_COLOR;
                    if (isLeader(member.getHousehold())) {
                        color = member.getDistrict() + LEADER_COLOR;
                        options.setZindex(1000);
                    }
                    options.setIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=" + color);
                }

                final Marker marker = Marker.newInstance(options);
                marker.setMap(mapWidget);
                markerSet.put(member.getHousehold(), marker);

                marker.addClickHandler(new MarkerHandler(marker, member));

            }
            // zoom map to bounds of markers
            // get first member
            Member member = memberList.get(0);
            double left = member.getLng();
            double top = member.getLat();
            double right = member.getLng();
            double bottom = member.getLat();
            for (Member m : memberList) {
                if(m.getLng() < left){
                    left = m.getLng();
                }
                if(m.getLng() > right){
                    right = m.getLng();
                }
                if(m.getLat() > top){
                    top = m.getLat();
                }
                if(m.getLat() < bottom){
                    bottom = m.getLat();
                }
            }
            LatLng ne = LatLng.newInstance(top, right);
            LatLng sw = LatLng.newInstance(bottom, left);
            LatLngBounds b = LatLngBounds.newInstance(sw, ne);
            mapWidget.panToBounds(b);
        }
    }

    private Iterable<Member> getMembersAtLocation(Member memberIn) {
        double lat = memberIn.getLat();
        double lng = memberIn.getLng();
        double range = 0.0005;
        List<Member> members = new ArrayList<Member>();
        for (Member member : memberList) {
            if (member.getLat() > lat - range && member.getLat() < lat + range
                    && member.getLng() > lng - range && member.getLng() < lng + range) {
                members.add(member);
            }
        }
        return members;
    }

    @UiHandler("load")
    public void loadData(ClickEvent event) {
        activity.goTo(new LoadPlace("loaddata"));
    }

    @UiHandler("compute")
    public void computeMembers(ClickEvent event) {
        activity.computeDistrictMembers();
    }

    @UiHandler("reports")
    public void reports(ClickEvent event) {
        activity.goTo(new DistrictPlace("lists"));
    }

    @UiHandler("districtCircle")
    public void showCircles(ClickEvent event) {
        if (districtCircle.getValue()) {
            for (District district : districtList) {
                Member leader = district.getLeader();
                if (leader != null) {
                    CircleOptions circleOptions = CircleOptions.newInstance();
                    LatLng latLng = LatLng.newInstance(leader.getLat(), leader.getLng());
                    circleOptions.setCenter(latLng);
                    circleOptions.setZindex(0);
                    circleOptions.setFillColor("222222");
                    circleOptions.setFillOpacity(.2);
                    circleOptions.setRadius(1000);
                    Circle circle = Circle.newInstance(circleOptions);
                    circle.setMap(mapWidget);
                    districtCircleList.add(circle);
                }
            }
            // get checkbox status, if checked
            // for each district get leader lat lng
            // create circle
            // add circle to list to erase later
        } else {
            // else remove circles
            for (Circle circle : districtCircleList) {
                circle.setMap(null);
            }
            districtCircleList.clear();
        }
    }

}
