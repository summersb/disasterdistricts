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
package org.lds.disasterlocator.client.load;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import org.lds.disasterlocator.client.ClientFactory;

/**
 *
 * @author Bert W Summers
 */
public class LoadActivity extends AbstractActivity implements LoadView.Activity{
    private final ClientFactory clientFactory;
    private final LoadView view;

    public LoadActivity(ClientFactory factory){
        clientFactory = factory;
        this.view = clientFactory.getLoadView();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setActivity(this);
        panel.setWidget(view);
    }

    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public AutoBeanFactory getAutoBeanFactory() {
        return clientFactory.getAutoBeanFactory();
    }

}