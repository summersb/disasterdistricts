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

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import java.util.ArrayList;
import org.lds.disasterlocator.client.map.MapPlace;

/**
 * Entry point classes define
 * <code>onModuleLoad()</code>.
 */
public class ProjectEntryPoint implements EntryPoint {

    private SimplePanel appWidget = new SimplePanel();
    private Place defaultPlace = new MapPlace("map");

    @Override
    public void onModuleLoad() {
        ClientFactory clientFactory = GWT.create(ClientFactory.class);
        EventBus eventBus = clientFactory.getEventBus();
        PlaceController placeController = clientFactory.getPlaceController();

        // Start ActivityManager for the main widget with our ActivityMapper
        ActivityMapper activityMapper = new AppActivityMapper(clientFactory);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appWidget);

        // Start PlaceHistoryHandler with our PlaceHistoryMapper
        AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, defaultPlace);

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
//                addScript("oms", "OverlappingMarkerSpiderfier.js");
                RootPanel.get().add(appWidget);
                // Goes to the place represented on URL else default place
                historyHandler.handleCurrentHistory();
            }
        };

        LoadApi.go(onLoad, loadLibraries, sensor);

    }
//    public void addScript(String uniqueId, String url) {
//        Element e = DOM.createElement("script");
//        DOM.setAttribute(e, "language", "JavaScript");
//        DOM.setAttribute(e, "src", url);
////        scriptTags.put(uniqueId, e);
//        DOM.appendChild(RootPanel.get().getElement(), e);
//    }
}
