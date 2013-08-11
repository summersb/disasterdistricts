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
package org.lds.disasterlocator.server.rest.json;

import java.util.List;
import org.lds.disasterlocator.server.rest.jpa.MemberJpa;

/**
 *
 * @author Bert W Summers
 */
public class DistrictCreateRequestContainer {

    private MemberJpa leader;
    private List<MemberJpa> members;
    private int districtId;

    @Override
    public String toString() {
        return "DistrictCreateRequestContainer{" + "leader=" + leader + ", members=" + members + ", districtId=" + districtId + '}';
    }


    /**
     * @return the leader
     */
    public MemberJpa getLeader() {
        return leader;
    }

    /**
     * @param leader the leader to set
     */
    public void setLeader(MemberJpa leader) {
        this.leader = leader;
    }

    /**
     * @return the members
     */
    public List<MemberJpa> getMembers() {
        return members;
    }

    /**
     * @param members the members to set
     */
    public void setMembers(List<MemberJpa> members) {
        this.members = members;
    }

    /**
     * @return the districtId
     */
    public int getDistrictId() {
        return districtId;
    }

    /**
     * @param districtId the districtId to set
     */
    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }
}
