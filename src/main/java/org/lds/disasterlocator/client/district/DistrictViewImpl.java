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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.events.resize.ResizeMapEvent;
import com.google.gwt.maps.client.events.resize.ResizeMapHandler;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.MyResources;
import org.lds.disasterlocator.client.map.MapPlace;
import org.lds.disasterlocator.shared.District;
import org.lds.disasterlocator.shared.Member;

/**
 *
 * @author Bert W Summers
 */
public class DistrictViewImpl extends Composite implements
        DistrictView {

    private static final Logger logger = Logger.getLogger(DistrictViewImpl.class.getName());
    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);
    @UiField
    HTMLPanel topMenu;
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

    @Override
    public void clearState() {
//        memberList = null;
//        districtList = null;
//        for (Circle circle : districtCircleList) {
//            circle.setMap(null);
//        }
//        districtCircleList.clear();
        Set<String> keySet = markerSet.keySet();
        for (String key : keySet) {
            Marker m = markerSet.get(key);
            m.setMap((MapWidget) null);
        }
        markerSet.clear();
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
    public void district(ChangeEvent event) {
        table.setVisible(false);
        clearState();
        map.setVisible(true);
        mapWidget.setSize(Window.getClientWidth() + "px", Window.getClientHeight() - 150 + "px");
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
        tableWidth = 4;

        grid = new Grid(tableHeight + 1, tableWidth);

        Label label = new Label("District #");
        grid.setWidget(0, 0, label);

        label = new Label("Leader");
        grid.setWidget(0, 1, label);

        label = new Label("Household Count");
        grid.setWidget(0, 2, label);
        
        label = new Label("Email");
        grid.setWidget(0, 2, label);


        int[] Count;
        Count = new int[districtList.size() + 1];
        for(Member member : memberList)
        {
            Count[member.getDistrict()]++;
        }

        for (int i = 0; i < districtList.size(); i++) {
            District district = districtList.get(i);
            String cell;
            cell = String.valueOf(district.getId());
            label = new Label(cell);
            grid.setWidget(i + 1, 0, label);

            cell = district.getLeader().getHousehold();
            label = new Label(cell);
            grid.setWidget(i + 1, 1, label);

            cell = String.valueOf(Count[district.getId()]);
            label = new Label(cell);
            grid.setWidget(i + 1, 2, label);
            
            cell = district.getLeader().getEmail();
            label = new Label(cell);
            grid.setWidget(i + 1, 3, label);
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

        fullTable(memberList);

        map.setVisible(false);
        table.setVisible(true);
        table.clear();
        table.add(grid);
    }

    private void fullTable(List<Member> list) {
        Collections.sort(list, new HouseholdSort());

        tableHeight = list.size();
        tableWidth = 4;

        grid = new Grid(tableHeight + 2, tableWidth);

        Label label = new Label("District #");
        grid.setWidget(0, 0, label);

        label = new Label("Household");
        grid.setWidget(0, 1, label);

        label = new Label("Address");
        grid.setWidget(0, 2, label);

        label = new Label("Phone");
        grid.setWidget(0, 3, label);
        int i;
        for (i = 0; i < list.size(); i++) {
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

        String cell;
        cell = "Household Count";
        label = new Label(cell);
        grid.setWidget(i + 1, 0, label);

        cell = String.valueOf(i);
        label = new Label(cell);
        grid.setWidget(i + 1, 1, label);

        table.setVisible(true);
        table.clear();
        table.add(grid);

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
                    mapWidget.setSize(Window.getClientWidth() + "px", Window.getClientHeight() - 150 + "px");
                }
            });

            mapWidget.setSize(Window.getClientWidth() + "px", Window.getClientHeight() - 150 + "px");

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
                        //mapWidget.setCenter(center);
                        //mapWidget.panTo(center);
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
            
            // zoom map to bounds of markers
            // get first member
            Member member = list.get(0);
            double left = member.getLng();
            double top = member.getLat();
            double right = member.getLng();
            double bottom = member.getLat();
            for (Member m : list) {
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
            
            fullTable(list);
        }
    }
}
