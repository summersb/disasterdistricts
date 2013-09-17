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
package org.lds.disasterlocator.client.district;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.maps.client.events.resize.ResizeMapEvent;
import com.google.gwt.maps.client.events.resize.ResizeMapHandler;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.services.Geocoder;
import com.google.gwt.maps.client.services.GeocoderGeometry;
import com.google.gwt.maps.client.services.GeocoderRequest;
import com.google.gwt.maps.client.services.GeocoderRequestHandler;
import com.google.gwt.maps.client.services.GeocoderResult;
import com.google.gwt.maps.client.services.GeocoderStatus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.MyResources;
import org.lds.disasterlocator.client.map.MapPlace;
import org.lds.disasterlocator.client.map.MapViewImpl;
import org.lds.disasterlocator.shared.District;
import org.lds.disasterlocator.shared.File;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MyConstants;
import org.lds.disasterlocator.shared.Row;

/**
 *
 * @author Bert W Summers
 */
public class DistrictViewImpl extends Composite implements
        DistrictView {

    private static final Logger logger = Logger.getLogger(DistrictViewImpl.class.getName());
    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);
    @UiField
    HTMLPanel panel;
    @UiField
    Button back;
    @UiField
    Button leaders;
    @UiField
    Button alphabetic;
    @UiField
    HTMLPanel table;
    @UiField
    MyResources res;
    @UiField
    ListBox district;
    @UiField
    HTMLPanel map;
    Grid legendGrid;
    private Activity activity;
    private int tableHeight;
    private int tableWidth;
    private List<Member> memberList;
    private List<District> districtList;
    private Grid grid;
    private MapWidget mapWidget;
    private Map<String, Marker> markerSet = new HashMap<String, Marker>();
    private static final String LEADER_COLOR = "L|00ff00|000000";
    private static final String MEMBER_COLOR = "|FF0000|000000";

    public DistrictViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        res.style().ensureInjected();
    }

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private String getTextBoxValue(int row, int household) {
        if (household == -1) {
            return "";
        }
        TextBox tb = (TextBox) grid.getWidget(row, household);
        if (tb == null) {
            return "";
        }
        return tb.getValue();
    }

    interface MapUiBinder extends UiBinder<Widget, DistrictViewImpl> {
    }

    @UiHandler("back")
    public void loadMap(ClickEvent event) {
        activity.goTo(new MapPlace("map"));
    }

    @UiHandler("leaders")
    public void leaders(ClickEvent event) {
        leaderTable();
    }

    @UiHandler("alphabetic")
    public void alpha(ClickEvent event) {
        alphaTable();
    }

    @UiHandler("district")
    public void district(ClickEvent event) {
        table.setVisible(false);
        map.setVisible(true);
        renderMap();
        plotHouses(district.getSelectedIndex() + 1);
    }

    @Override
    public void setDistricts(List<District> list) {
        districtList = list;
        district.clear();
        for (District item : districtList) {
            district.addItem("District " + String.valueOf(item.getId()));
        }
    }

    @Override
    public void setMembers(List<Member> members) {
        memberList = members;
    }

    private void leaderTable() {
        tableHeight = districtList.size();
        tableWidth = 2;

        grid = new Grid(tableHeight + 1, tableWidth);

        Label label = new Label("District #");
        grid.setWidget(0, 0, label);

        label = new Label("Leader");
        grid.setWidget(0, 1, label);

        for (int i = 0; i < districtList.size(); i++) {
            District district = districtList.get(i);
            String cell;
            cell = String.valueOf(district.getId());
            label = new Label(cell);
            grid.setWidget(i + 1, 0, label);

            cell = district.getLeader().getHousehold();
            label = new Label(cell);
            grid.setWidget(i + 1, 1, label);

        }
        map.setVisible(false);
        table.setVisible(true);
        table.clear();
        table.add(grid);
    }

    class HouseholdSort implements Comparator<Member> {

        @Override
        public int compare(Member o1, Member o2) {
            String name1 = o1.getHousehold();
            String name2 = o2.getHousehold();
            return name1.compareTo(name2);
        }
    }

    private void alphaTable() {

        Collections.sort(memberList, new HouseholdSort());

        tableHeight = memberList.size();
        tableWidth = 2;

        grid = new Grid(tableHeight + 1, tableWidth);

        Label label = new Label("District #");
        grid.setWidget(0, 0, label);

        label = new Label("Household");
        grid.setWidget(0, 1, label);

        for (int i = 0; i < memberList.size(); i++) {
            Member member = memberList.get(i);
            String cell;
            cell = String.valueOf(member.getDistrict());
            label = new Label(cell);
            grid.setWidget(i + 1, 0, label);

            cell = member.getHousehold();
            label = new Label(cell);
            grid.setWidget(i + 1, 1, label);

        }
        map.setVisible(false);
        table.setVisible(true);
        table.clear();
        table.add(grid);
    }

    private void fullTable(List<Member> list) {
        Collections.sort(list, new HouseholdSort());

        tableHeight = list.size();
        tableWidth = 4;

        grid = new Grid(tableHeight + 1, tableWidth);

        Label label = new Label("District #");
        grid.setWidget(0, 0, label);

        label = new Label("Household");
        grid.setWidget(0, 1, label);
        
        label = new Label("Address");
        grid.setWidget(0, 2, label);
        
        label = new Label("Phone");
        grid.setWidget(0, 3, label);

        for (int i = 0; i < list.size(); i++) {
            Member member = list.get(i);
            String cell;
            cell = String.valueOf(member.getDistrict());
            label = new Label(cell);
            grid.setWidget(i + 1, 0, label);

            cell = member.getHousehold();
            label = new Label(cell);
            grid.setWidget(i + 1, 1, label);
            
            cell = member.getAddress();
            label = new Label(cell);
            grid.setWidget(i + 1, 2, label);
            
            cell = member.getPhone();
            label = new Label(cell);
            grid.setWidget(i + 1, 3, label);

        }
        table.setVisible(true);
        table.clear();
        table.add(grid);

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

    public void plotHouses(int id) {
        if (memberList != null && districtList != null) {
            List<Member> list;
            list = new ArrayList<Member>();
            
            for (final Member member : memberList) {
                LatLng center = LatLng.newInstance(member.getLat(), member.getLng());
                MarkerOptions options = MarkerOptions.newInstance();
                options.setPosition(center);
                options.setTitle(rollOver(member));
                if (member.getDistrict() != 0) {
                    // we can set district colors here also
                    String color = member.getDistrict() + MEMBER_COLOR;
                    if (isLeader(member.getHousehold())) {
                        color = LEADER_COLOR;
                        options.setZindex(1000);
                        mapWidget.setCenter(center);
                    }
                    options.setIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=" + color);
                }

                if (member.getDistrict() == id) {
                    final Marker marker = Marker.newInstance(options);
                    marker.setMap(mapWidget);
                    markerSet.put(member.getHousehold(), marker);
                    list.add(member);
                }

                //marker.addClickHandler(new MapViewImpl.MarkerHandler(marker, member));
            }
            Set<String> memberHousehold = markerSet.keySet();
            for (String household : memberHousehold) {
                if (isLeader(household)) {
                    Marker marker = markerSet.get(household);
                    marker.setIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=" + LEADER_COLOR);
                    marker.setZindex(1000);
                }
            }
            fullTable(list);
        }
    }
}
