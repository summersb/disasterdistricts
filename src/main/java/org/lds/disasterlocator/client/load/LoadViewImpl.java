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
package org.lds.disasterlocator.client.load;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.maps.client.base.LatLng;
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
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.map.MapPlace;
import org.lds.disasterlocator.shared.File;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.Row;

/**
 *
 * @author Bert W Summers
 */
public class LoadViewImpl extends Composite implements LoadView {

    private static final Logger logger = Logger.getLogger(LoadViewImpl.class.getName());
    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);
    @UiField
    HTMLPanel panel;
    @UiField
    Button map;
    @UiField
    FormPanel form;
    @UiField
    Button submit;
    @UiField
    FileUpload fileUpload;
    @UiField
    HTMLPanel table;
    @UiField
    Button process;
    private Activity activity;
    private List<ListBox> listBoxList = new ArrayList<ListBox>();
    private int tableHeight;
    private int tableWidth;
    private Grid grid;

    public LoadViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private ListBox createList() {
        // add heading row to select which column is what
        ListBox lb = new ListBox(false);
        lb.addItem("None");
        lb.addItem("Household (unique)");
        lb.addItem("Address");
        lb.addItem("City");
        lb.addItem("State");
        lb.addItem("zip");
        lb.addItem("email");
        lb.addItem("phone");
        lb.addChangeHandler(new ListBoxChangeHandler(this));
        listBoxList.add(lb);
        return lb;
    }

    interface MapUiBinder extends UiBinder<Widget, LoadViewImpl> {
    }

    @UiHandler("map")
    public void loadMap(ClickEvent event) {
        activity.goTo(new MapPlace("map"));
    }

    @UiHandler("form")
    public void formSubmit(FormPanel.SubmitCompleteEvent event) {
        // upload is complete
        logger.info("form submit complete");
        String data = event.getResults();
        // now send this to render table
        createTable(data);
        form.setVisible(false);
    }

    @UiHandler("submit")
    public void submitCLicked(ClickEvent event) {
        logger.info("submit clicked");
        form.submit();
    }

    /**
     * This method assumes that at least household and address have been
     * selected
     *
     * @param event
     */
    @UiHandler("process")
    public void processFile(ClickEvent event) {
        int household = -1, address = -1;
        for (int i = 0; i < listBoxList.size(); i++) {
            ListBox listBox = listBoxList.get(i);
            int index = listBox.getSelectedIndex();
            if (index == 1) {
                // household ok
                household = i;
            } else if (index == 2) {
                // address ok
                address = i;
            }
        }
        if (household > -1 && address > -1) {
            // geocode address row by row
            AutoBeanFactory factory = activity.getAutoBeanFactory();

            // create a queue to process addresses
            Queue<Member> queue = new LinkedList<Member>();
            for (int row = 1; row <= tableHeight; row++) {
                AutoBean<Member> memberAB = factory.create(Member.class);
                final Member member = memberAB.as();
                member.setHousehold(grid.getText(row, household));
                member.setAddress(grid.getText(row, address));
                queue.add(member);
            }
            // process queue, on query over limit create a timer to delay processing
            // remove failed address from queue
            GeocoderRequestHandler grh = new MyGeoRequestHandler(queue, grid, new MyGeoRequestHandler.CallBack() {

                @Override
                public void complete() {
                    // now we can remove grey rows from table
                    logger.info("Clean up time");
                    HTMLTable.RowFormatter rowFormatter = grid.getRowFormatter();
                    for (int i = tableHeight; i >=1; i--) {
                        String styleName = rowFormatter.getStyleName(i);
                        if("grey".equals(styleName)){
                            grid.removeRow(i);
                        }
                    }
                    tableHeight = grid.getRowCount()-1;
                }
            });
        } else {
            Window.alert("You must select at least household and address to continue");
        }
    }

    private void createTable(String data) {
        AutoBeanFactory factory = activity.getAutoBeanFactory();
        AutoBean<File> fileAB = AutoBeanCodex.decode(factory, File.class, data);
        File file = fileAB.as();

        tableHeight = file.getRows().size();
        tableWidth = file.getRows().get(0).getCells().size();

        List<Row> rows = file.getRows();
        grid = new Grid(tableHeight + 1, tableWidth);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            List<String> cells = row.getCells();
            for (int column = 0; column < cells.size(); column++) {
                String cell = cells.get(column);
                grid.setText(i + 1, column, cell);
            }
        }
        listBoxList.clear();
        for (int i = 0; i < tableWidth; i++) {
            grid.setWidget(0, i, createList());
        }
        table.add(grid);
    }

    private static class ListBoxChangeHandler implements ChangeHandler {

        private final LoadViewImpl page;

        public ListBoxChangeHandler(LoadViewImpl page) {
            this.page = page;
        }

        @Override
        public void onChange(ChangeEvent event) {
            // check that no other list has this item selected
            // if so then unselect it
            ListBox source = (ListBox) event.getSource();
            int selectedIndex = source.getSelectedIndex();
            for (ListBox lb : page.listBoxList) {
                if (lb.equals(source)) {
                    continue;
                }
                if (selectedIndex == lb.getSelectedIndex()) {
                    lb.setSelectedIndex(0);
                }
            }
        }
    }

    static class MyGeoRequestHandler implements GeocoderRequestHandler {

        private Member member;
        private final Grid grid;
        private final Queue<Member> queue;
        private final CallBack callback;
        private int row = 1; // row 0 is listbox row

        public MyGeoRequestHandler(Queue<Member> queue, Grid grid, CallBack callback) {
            this.queue = queue;
            this.grid = grid;
            this.callback = callback;
            processMember();
        }

        @Override
        public void onCallback(JsArray<GeocoderResult> results, GeocoderStatus status) {
            if (status.toString().equals("OK")) {
                if (results.length() != 1) {
                    logger.severe("Incorrect results for address " + member.getAddress());
                    end();
                    // error. to many results need to clarify address
                    return;
                }
                GeocoderResult gr = results.get(0);
                GeocoderGeometry geometry = gr.getGeometry();
                LatLng location = geometry.getLocation();
                member.setAddress(gr.getFormatted_Address());
                member.setLat(Double.toString(location.getLatitude()));
                member.setLng(Double.toString(location.getLongitude()));
                // when geocode is complete send row to server with lat/long to insert record

                // when complete delete row from table
                // threading issue instead mark row with style
//                grid.removeRow(row);
                grid.getRowFormatter().setStyleName(row, "grey");
                logger.info("Process " + member.getAddress());
                end();
            } else if ("OVER_QUERY_LIMIT".equals(status.toString())) {
                // need to delay here
                logger.info("Over query limit, pausing for 10 seconds");
                Timer t = new Timer() {
                    @Override
                    public void run() {
                        processMember();
                    }
                };
                t.schedule(10000);
            } else {
                logger.severe("Failed to process " + status.toString() + " for " + member.getAddress());
                end();
            }
        }

        private void processMember() {
            this.member = queue.peek();
            if (member == null) {
                callback.complete();
                return;
            }
            Geocoder geocoder = Geocoder.newInstance();
            GeocoderRequest geoRequest = GeocoderRequest.newInstance();
            geoRequest.setAddress(member.getAddress());
            geocoder.geocode(geoRequest, this);
        }

        private void end() {
            row++;
            queue.remove();
            processMember();
            return;
        }

        interface CallBack {

            void complete();
        }
    }
}