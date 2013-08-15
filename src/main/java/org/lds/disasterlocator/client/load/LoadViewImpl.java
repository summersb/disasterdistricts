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
package org.lds.disasterlocator.client.load;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.ClientFactory;
import org.lds.disasterlocator.client.map.MapPlace;
import org.lds.disasterlocator.shared.File;
import org.lds.disasterlocator.shared.Row;

/**
 *
 * @author Bert W Summers
 */
public class LoadViewImpl extends Composite implements LoadView{

    private static final Logger logger = Logger.getLogger(LoadViewImpl.class.getName());
    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);

    @UiField HTMLPanel panel;
    @UiField Button map;
    @UiField FormPanel form;
    @UiField Button submit;
    @UiField FileUpload fileUpload;
    @UiField HTMLPanel table;

    private Activity activity;
    private List<ListBox> listBoxList = new ArrayList<ListBox>();

    public LoadViewImpl(){
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
    public void loadMap(ClickEvent event){
        activity.goTo(new MapPlace("map"));
    }

    @UiHandler("form")
    public void formSubmit(FormPanel.SubmitCompleteEvent event){
        // upload is complete
        logger.info("form submit complete");
        String data = event.getResults();
        // now send this to render table
        createTable(data);
    }

    @UiHandler("submit")
    public void submitCLicked(ClickEvent event){
        logger.info("submit clicked");
        form.submit();
    }

    private void createTable(String data) {
            AutoBeanFactory factory = activity.getAutoBeanFactory();
            AutoBean<File> fileAB = AutoBeanCodex.decode(factory, File.class, data);
            File file = fileAB.as();

            int tableHeight = file.getRows().size();
            int tableWidth = file.getRows().get(0).getCells().size();

            List<Row> rows = file.getRows();
            Grid grid = new Grid(tableHeight+1, tableWidth);
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                List<String> cells = row.getCells();
                for (int column = 0; column < cells.size(); column++) {
                    String cell = cells.get(column);
                    grid.setWidget(i+1, column, new Label(cell));
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
                if(lb.equals(source)){
                    continue;
                }
                if(selectedIndex == lb.getSelectedIndex()){
                    lb.setSelectedIndex(0);
                }
            }
        }
    }
}