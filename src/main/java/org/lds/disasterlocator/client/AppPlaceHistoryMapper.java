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
/**
 * The U. S. Government has "Government Purpose Rights" to this technical data in accordance
 * with DFARS 252.227-7013 Rights in Technical Data - Noncommercial Items and
 * DFARS 252.227-7014 Rights in Noncommercial Computer Software and Noncommercial
 * Computer Software Documentation (JUN 2013).
 *
 * Branch of DoD: USMC
 * Project Name: D2 BCD
 * Contract: M67854-13-C-6584
 * Contractor Name: Power Ten, Inc.
 * Contractor Address: 10422 NE 37th Circle Building #6, Kirkland, WA 98033
 * Expiration Date: Government Purpose Rights expire on April 19, 2016
 */
package org.lds.disasterlocator.client;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import org.lds.disasterlocator.client.load.LoadPlace;
import org.lds.disasterlocator.client.map.MapPlace;

/**
 *
 * @author Bert W Summers
 */
@WithTokenizers({MapPlace.Tokenizer.class, LoadPlace.Tokenizer.class})
public interface AppPlaceHistoryMapper extends PlaceHistoryMapper{

}
