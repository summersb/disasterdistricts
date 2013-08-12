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

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import org.lds.disasterlocator.client.load.LoadActivity;
import org.lds.disasterlocator.client.load.LoadPlace;
import org.lds.disasterlocator.client.map.MapActivity;
import org.lds.disasterlocator.client.map.MapPlace;

/**
 *
 * @author Bert W Summers
 */
public class AppActivityMapper implements ActivityMapper{
    private final ClientFactory clientFactory;

    public AppActivityMapper(ClientFactory factory){
        super();
        clientFactory = factory;
    }

    @Override
    public Activity getActivity(Place place) {
        if(place instanceof MapPlace){
            return new MapActivity(clientFactory);
        }else if(place instanceof LoadPlace){
            return new LoadActivity(clientFactory);
        }
        return null;
    }

}
