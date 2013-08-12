/**
 * Copyright (C) 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lds.disasterlocator.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import org.lds.disasterlocator.client.load.LoadView;
import org.lds.disasterlocator.client.load.LoadViewImpl;
import org.lds.disasterlocator.client.map.MapView;
import org.lds.disasterlocator.client.map.MapViewImpl;
import org.lds.disasterlocator.shared.MyAutoBeanFactory;

/**
 *
 * @author Bert W Summers
 */
public class ClientFactoryImpl implements ClientFactory{
    private final EventBus eventBus = new SimpleEventBus();
    private final AutoBeanFactory myAutoBeanFactory = GWT.create(MyAutoBeanFactory.class);
    private final PlaceController placeController = new PlaceController(eventBus);
    private final MapView mapView = new MapViewImpl();
    private final LoadView loadView = new LoadViewImpl();

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public AutoBeanFactory getAutoBeanFactory() {
        return myAutoBeanFactory;
    }

    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }

    @Override
    public MapView getMapView() {
        return mapView;
    }

    @Override
    public LoadView getLoadView() {
        return loadView;
    }
}
