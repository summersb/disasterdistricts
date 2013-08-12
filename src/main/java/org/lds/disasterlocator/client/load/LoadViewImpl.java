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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.lds.disasterlocator.client.ClientFactory;
import org.lds.disasterlocator.client.map.MapPlace;

/**
 *
 * @author Bert W Summers
 */
public class LoadViewImpl extends Composite implements LoadView{

    private static MapUiBinder uiBinder = GWT.create(MapUiBinder.class);

    @UiField HTMLPanel panel;
    @UiField Button map;

    private ClientFactory clientFactory;
    private Activity activity;

    public LoadViewImpl(){
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setClientFactory(ClientFactory factory){
        this.clientFactory = factory;
    }

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    interface MapUiBinder extends UiBinder<Widget, LoadViewImpl> {
    }

    @UiHandler("map")
    public void loadMap(ClickEvent event){
        activity.goTo(new MapPlace("map"));
    }

}