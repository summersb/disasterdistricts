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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.map.MapPlace;
import org.lds.disasterlocator.shared.File;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MyConstants;
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
                TextBox tb = (TextBox) grid.getWidget(row, household);
                if(tb == null){
                    // must have been a blank line in the source file
                    grid.removeRow(row);
                    row--;
                    continue;
                }
                member.setHousehold(tb.getValue());
                tb = (TextBox) grid.getWidget(row, address);
                member.setAddress(tb.getValue());
                queue.add(member);
            }
            // process queue, on query over limit create a timer to delay processing
            // remove failed address from queue
            GeocoderRequestHandler grh = new MyGeoRequestHandler(queue, grid, new CallBack() {
                @Override
                public void complete() {
                    // now we can remove grey rows from table
                    logger.info("Clean up time");
                    // ask user to fix errors
                    tableHeight = grid.getRowCount() - 1;
                }
            }, this);
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
        grid = new Grid(tableHeight+1, tableWidth);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            List<String> cells = row.getCells();
            for (int column = 0; column < cells.size(); column++) {
                String cell = cells.get(column);
                TextBox textBox = new TextBox();
                textBox.setText(cell);
                grid.setWidget(i + 1, column, textBox);
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

    static class MyGeoRequestHandler implements GeocoderRequestHandler, RestCallBack {

        private Member member;
        private final Grid grid;
        private final Queue<Member> queue;
        private final CallBack callback;
        private int row = 1; // row 0 is listbox row
        private final AutoBeanFactory factory;
        private String household;

        public MyGeoRequestHandler(Queue<Member> queue, Grid grid, CallBack callback, LoadViewImpl page) {
            this.queue = queue;
            this.grid = grid;
            this.callback = callback;
            factory = page.activity.getAutoBeanFactory();
            processMember();
        }

        @Override
        public void onCallback(JsArray<GeocoderResult> results, GeocoderStatus status) {
            if (status.toString().equals("OK")) {
                if (results.length() != 1) {
                    logger.severe("Incorrect results for address " + member.getHousehold() + ":" + member.getAddress());
                    grid.getRowFormatter().setStyleName(row, "badaddress");
                    row++;
                    end();
                    // error. to many results need to clarify address
                } else {
                    GeocoderResult gr = results.get(0);
                    GeocoderGeometry geometry = gr.getGeometry();
                    LatLng location = geometry.getLocation();
                    member.setAddress(gr.getFormatted_Address());
                    member.setLat(Double.toString(location.getLatitude()));
                    member.setLng(Double.toString(location.getLongitude()));
                    // when geocode is complete send row to server with lat/long to insert record
                    AutoBean<Member> memberAB = factory.create(Member.class, member);
                    String json = AutoBeanCodex.encode(memberAB).getPayload();
                    try {
                        RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, MyConstants.REST_URL + "member");
                        rb.setHeader(MyConstants.CONTENT_TYPE, MyConstants.APPLICATION_JSON);
                        rb.sendRequest(json, new MyRequestCallbackHandler(this));
                    } catch (RequestException ex) {
                        logger.log(Level.SEVERE, "Failed to persist member" + member.getHousehold() + ":" + member.getAddress(), ex);
                        grid.getRowFormatter().setStyleName(row, "failed");
                        row++;
                        end();
                    }
                }
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
                logger.severe("Failed to process " + status.toString() + " for " + member.getHousehold() + ":" + member.getAddress());
                grid.getRowFormatter().setStyleName(row, "badaddress");
                row++;
                end();
            }
        }

        private void processMember() {
            this.member = queue.peek();
            if (member == null) {
                callback.complete();
            }else{
                household = member.getHousehold();
                Geocoder geocoder = Geocoder.newInstance();
                GeocoderRequest geoRequest = GeocoderRequest.newInstance();
                geoRequest.setAddress(member.getAddress());
                geocoder.geocode(geoRequest, this);
            }
        }

        private void end() {
            queue.remove();
            processMember();
        }

        @Override
        public void success() {
            grid.removeRow(row);
            logger.info("Process " + member.getHousehold() + ":" + member.getAddress());
            end();
        }

        @Override
        public void failure() {
            logger.info("Failed to process " + member.getHousehold() + ":" + member.getAddress());
            grid.getRowFormatter().setStyleName(row, "duplicate");
            row++;
            end();
        }
    }

    static class MyRequestCallbackHandler implements RequestCallback{
        private final RestCallBack callback;

        MyRequestCallbackHandler(RestCallBack callback){
            this.callback = callback;
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == MyConstants.OK){
                callback.success();
            }else{
                logger.info("Failed from persist call with code " + response.getStatusCode());
                callback.failure();
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            callback.failure();
        }

    }

    interface CallBack {
        void complete();
    }

    interface RestCallBack{
        void success();
        void failure();
    }
}