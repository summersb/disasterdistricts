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
import org.lds.disasterlocator.server.rest.jpa.MemberJpa;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MyAutoBeanFactory;

/**
 *
 * @author Bert Summers
 */
public class MemberResourceTest {

    private MemberResource memberResource;
    private MyAutoBeanFactory factory;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("disaster-test");
        memberResource = new MemberResource(emf);
        factory = AutoBeanFactorySource.create(MyAutoBeanFactory.class);
    }

    @After
    public void tearDown(){
        List<MemberJpa> wardList = memberResource.getWardList();
        for (MemberJpa memberJpa : wardList) {
            memberResource.deleteMember(memberJpa.getHousehold());
        }
    }
    @Test
    public void testGetWardList() {
        List<MemberJpa> wardList = memberResource.getWardList();
        assertEquals(0, wardList.size());
        testCreateMember();
        wardList = memberResource.getWardList();
        assertEquals(1, wardList.size());
    }

    @Test
    public void testUpdateMember() throws Exception {
        AutoBean<Member> memberab = factory.create(Member.class);
        Member member = memberab.as();
        member.setHousehold("bob");
        String json = AutoBeanCodex.encode(memberab).getPayload();
        Response response = memberResource.createMember(json);
        assertEquals(200, response.getStatus());
        member.setAddress("test");
        member.setAuto(false);
        json = AutoBeanCodex.encode(memberab).getPayload();
        memberResource.updateMember(json);
        response = memberResource.getMember("bob");
        assertEquals(200, response.getStatus());
        member = (Member) response.getEntity();
        assertEquals("test", member.getAddress());
        assertEquals(false, member.getAuto());
    }

    @Test
    public void testCreateMember() {
        AutoBean<Member> memberab = factory.create(Member.class);
        Member member = memberab.as();
        member.setHousehold("bob");
        String json = AutoBeanCodex.encode(memberab).getPayload();
        Response response = memberResource.createMember(json);
        assertEquals(200, response.getStatus());
        response = memberResource.getMember("bob");
        assertEquals(200, response.getStatus());
        Object entity = response.getEntity();
        Member m = (Member) entity;
        assertEquals("bob", m.getHousehold());
    }

    @Test
    public void testCreateExisting(){
        AutoBean<Member> memberab = factory.create(Member.class);
        Member member = memberab.as();
        member.setHousehold("bob");
        String json = AutoBeanCodex.encode(memberab).getPayload();
        Response response = memberResource.createMember(json);
        assertEquals(200, response.getStatus());
        response = memberResource.createMember(json);
        assertEquals(409, response.getStatus());
    }

    @Test
    public void testMissingMember() {
        Response response = memberResource.getMember("missing");
        assertEquals(404, response.getStatus());
        AutoBean<Member> memberab = factory.create(Member.class);
        Member missingMember = memberab.as();
        missingMember.setHousehold("missing");
        String json = AutoBeanCodex.encode(memberab).getPayload();
        response = memberResource.updateMember(json);
        assertEquals(404, response.getStatus());
        memberResource.deleteMember("missing");
        assertEquals(404, response.getStatus());
    }
}