/*
 * Copyright (C) 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lds.disasterlocator.server.rest;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.lds.disasterlocator.server.rest.jpa.DistrictJpa;
import org.lds.disasterlocator.server.rest.jpa.MemberJpa;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MyAutoBeanFactory;

/**
 *
 * @author Bert Summers
 */
public class DistrictResourceTest {

    DistrictResource service;
    MemberResource memberResource;
    private MyAutoBeanFactory factory;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("disaster-test");
        service = new DistrictResource(emf);
        memberResource = new MemberResource(emf);
        createMember("bob");
        createMember("fred");
    }

    @After
    public void tearDown(){
        List<DistrictJpa> list = service.getDistrict();
        for (DistrictJpa districtJpa : list) {
            Member leader = districtJpa.getLeader();
                service.deleteDistrict(leader.getHousehold());
        }
        List<MemberJpa> wardList = memberResource.getWardList();
        for (MemberJpa memberJpa : wardList) {
            memberResource.deleteMember(memberJpa.getHousehold());
        }

    }

    @Test
    public void testGetDistrict() {
        DistrictJpa newDistrict = service.createNewDistrict("bob");
        MemberJpa member = (MemberJpa) memberResource.getMember("bob").getEntity();
        assertEquals(newDistrict.getId(), member.getDistrict());
        List<DistrictJpa> district = service.getDistrict();
        assertEquals(1, district.size());
        assertEquals("bob", district.get(0).getLeader().getHousehold());
    }

    @Test
    public void testCreateNewDistrict_String_String() {
        service.createNewDistrict("bob", "fred");
        List<DistrictJpa> district = service.getDistrict();
        assertEquals(1, district.size());
        assertEquals("bob", district.get(0).getLeader().getHousehold());
        assertEquals("fred", district.get(0).getAssistant().getHousehold());
        MemberJpa member = (MemberJpa) memberResource.getMember("bob").getEntity();
        assertEquals(1, member.getDistrict());
        member = (MemberJpa) memberResource.getMember("fred").getEntity();
        assertEquals(1, member.getDistrict());
    }

    @Test
    public void testDeleteDistrict() {
        DistrictJpa newDistrict = service.createNewDistrict("bob");
        MemberJpa fred = (MemberJpa) memberResource.getMember("fred").getEntity();
        fred.setDistrict(newDistrict.getId());
        memberResource.updateMember(fred);
        MemberJpa member = (MemberJpa) memberResource.getMember("bob").getEntity();
        assertEquals(newDistrict.getId(), member.getDistrict());
        service.deleteDistrict("bob");
        // test that fred is not in district anymore
        fred = (MemberJpa) memberResource.getMember("fred").getEntity();
        assertEquals(0, fred.getDistrict());
        member = (MemberJpa) memberResource.getMember("bob").getEntity();
        assertEquals(0, member.getDistrict());
        List<DistrictJpa> district = service.getDistrict();
        assertEquals(0, district.size());
        List<MemberJpa> wardList = memberResource.getWardList();
        assertEquals(2, wardList.size());
        // test delete with assistant
        service.createNewDistrict("bob", "fred");
        member = (MemberJpa) memberResource.getMember("bob").getEntity();
        assertEquals(1, member.getDistrict());
        member = (MemberJpa) memberResource.getMember("fred").getEntity();
        assertEquals(1, member.getDistrict());
        service.deleteDistrict("bob");
        member = (MemberJpa) memberResource.getMember("bob").getEntity();
        assertEquals(0, member.getDistrict());
        member = (MemberJpa) memberResource.getMember("fred").getEntity();
        assertEquals(0, member.getDistrict());
        district = service.getDistrict();
        assertEquals(0, district.size());
        wardList = memberResource.getWardList();
        assertEquals(2, wardList.size());

    }

    @Test
    public void testDistrictDeleteAndRecreate(){
        // create district 1
        DistrictJpa bobDist = service.createNewDistrict("bob");
        // create district 2
        DistrictJpa fredDist = service.createNewDistrict("fred");
        // add members to 1 and 2
        MemberJpa john = createMember("john");
        MemberJpa joe = createMember("joe");
        // delete district 1
        service.deleteDistrict("bob");
        // create new district, should be district 1
        DistrictJpa dist = service.createNewDistrict("bob");
        assertEquals(1, dist.getId());
        MemberJpa member = (MemberJpa) memberResource.getMember("john").getEntity();
        assertEquals(0, member.getDistrict());
    }

    private MemberJpa createMember(String household){
        factory = AutoBeanFactorySource.create(MyAutoBeanFactory.class);
        AutoBean<Member> memberab = factory.create(Member.class);
        Member member = memberab.as();
        member.setHousehold(household);
        String json = AutoBeanCodex.encode(memberab).getPayload();
        memberResource.createMember(json);
        MemberJpa mem = (MemberJpa) memberResource.getMember(household).getEntity();
        return mem;
    }
}