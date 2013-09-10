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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import java.util.List;
import org.lds.disasterlocator.client.ClientFactory;
import org.lds.disasterlocator.shared.District;
import org.lds.disasterlocator.shared.Member;

/**
 *
 * @author Bert W Summers
 */
public interface MapView extends IsWidget {

    void setActivity(Activity activity);
    void plotHouses(List<Member> members);
    void renderMap();
    void setDistricts(List<District> districtlist);

    interface Activity {

        void goTo(Place place);
        AutoBeanFactory getAutoBeanFactory();
        ClientFactory getClientFactory();
        boolean isLeader(Member member);
        void setLeader(Member member);
        void setAuto(Member member);
        void computeDistrictMembers();
    }
}
