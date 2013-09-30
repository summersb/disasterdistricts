/**
 * Copyright (C) 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.lds.disasterlocator.client.district;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lds.disasterlocator.client.ClientFactory;
import org.lds.disasterlocator.client.map.MapActivity;
import org.lds.disasterlocator.shared.District;
import org.lds.disasterlocator.shared.DistrictList;
import org.lds.disasterlocator.shared.File;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MemberList;
import org.lds.disasterlocator.shared.MyConstants;
import org.lds.disasterlocator.shared.Row;

/**
 *
 * @author Bert W Summers
 */
public class DistrictActivity extends AbstractActivity implements DistrictView.Activity {

    private final ClientFactory clientFactory;
    private final DistrictView view;
    private List<District> districtList;
    private List<Member> members;
    private static final Logger logger = Logger.getLogger(DistrictActivity.class.getName());

    public DistrictActivity(ClientFactory factory) {
        clientFactory = factory;
        this.view = clientFactory.getDistrictView();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setActivity(this);
        panel.setWidget(view);
        view.renderMap();
        view.clearState();
        loadMemberData();
        loadDistrictData();


    }

    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public AutoBeanFactory getAutoBeanFactory() {
        return clientFactory.getAutoBeanFactory();
    }

    private void loadMemberData() {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, MyConstants.REST_URL + "member/list?stopevilcaching=" + new Date().getTime());
        rb.setHeader(MyConstants.CONTENT_TYPE, MyConstants.APPLICATION_JSON);
        try {
            rb.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {

                    String json = response.getText();
                    AutoBeanFactory autoBeanFactory = clientFactory.getAutoBeanFactory();
                    AutoBean<MemberList> memberListAB = AutoBeanCodex.decode(autoBeanFactory, MemberList.class, "{\"members\":" + json + "}");
                    MemberList memberList = memberListAB.as();
                    view.setMembers(memberList.getMembers());
                    members = memberList.getMembers();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error occured " + exception.getLocalizedMessage());
                }
            });
        } catch (RequestException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void loadDistrictData() {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, MyConstants.REST_URL + "district/list?stopevilcaching=" + new Date().getTime());
        rb.setHeader(MyConstants.CONTENT_TYPE, MyConstants.APPLICATION_JSON);
        try {
            rb.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {

                    String json = response.getText();
                    AutoBeanFactory autoBeanFactory = clientFactory.getAutoBeanFactory();
                    AutoBean<DistrictList> districtListAB = AutoBeanCodex.decode(autoBeanFactory, DistrictList.class, "{\"districts\":" + json + "}");
                    DistrictList districtlist = districtListAB.as();
                    districtList = districtlist.getDistricts();
                    view.setDistricts(districtlist.getDistricts());
                }

                @Override
                public void onError(Request request, Throwable exception) {
                }
            });
        } catch (RequestException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
