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
package org.lds.disasterlocator.client.map;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 *
 * @author Bert W Summers
 */
public class MapPlace extends Place{
    private String name;

    public MapPlace(String token){
        this.name = token;
    }

    public String getMapViewName(){
        return name;
    }

    public static class Tokenizer implements PlaceTokenizer<MapPlace>{

        @Override
        public MapPlace getPlace(String token) {
            return new MapPlace(token);
        }

        @Override
        public String getToken(MapPlace place) {
            return place.getMapViewName();
        }
    }
}
