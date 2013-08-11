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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.ArrayList;

/**
 * Entry point classes define
 * <code>onModuleLoad()</code>.
 */
public class ProjectEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        final ClientFactory clientFactory = GWT.create(ClientFactory.class);
        boolean sensor = false;

        // load all the libs for use in the maps
        ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
//        loadLibraries.add(LoadLibrary.ADSENSE);
        loadLibraries.add(LoadLibrary.DRAWING);
        loadLibraries.add(LoadLibrary.GEOMETRY);
//        loadLibraries.add(LoadLibrary.PANORAMIO);
//        loadLibraries.add(LoadLibrary.PLACES);
//        loadLibraries.add(LoadLibrary.WEATHER);
        loadLibraries.add(LoadLibrary.VISUALIZATION);

        Runnable onLoad = new Runnable() {
            @Override
            public void run() {
                addScript("oms", "OverlappingMarkerSpiderfier.js");
                Map map = new Map();
                map.setClientFactory(clientFactory);
                RootPanel.get().add(map);
            }
        };

        LoadApi.go(onLoad, loadLibraries, sensor);

    }

    public void addScript(String uniqueId, String url) {
        Element e = DOM.createElement("script");
        DOM.setAttribute(e, "language", "JavaScript");
        DOM.setAttribute(e, "src", url);
//        scriptTags.put(uniqueId, e);
        DOM.appendChild(RootPanel.get().getElement(), e);
    }
}
