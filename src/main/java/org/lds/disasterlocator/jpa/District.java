/**
 * Copyright (C) 2012
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
package org.lds.disasterlocator.jpa;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Bert W Summers
 */
@Entity
@Table(name = "District")
public class District implements Serializable {

    @Id
    private int id;

    @Override
    public String toString() {
        return "District{" + "id=" + getId() + ", leader=" + getLeader() + ", assistant=" + getAssistant() + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.getId();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final District other = (District) obj;
        if (this.getId() != other.getId()) {
            return false;
        }
        return true;
    }
    @OneToOne(cascade= CascadeType.ALL)
    private Member leader;
    @OneToOne(cascade= CascadeType.ALL)
    private Member assistant;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the leader
     */
    public Member getLeader() {
        return leader;
    }

    /**
     * @param leader the leader to set
     */
    public void setLeader(Member leader) {
        this.leader = leader;
    }

    /**
     * @return the assistant
     */
    public Member getAssistant() {
        return assistant;
    }

    /**
     * @param assistant the assistant to set
     */
    public void setAssistant(Member assistant) {
        this.assistant = assistant;
    }
    private final static ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);

    static {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @JsonCreator
    public static District create(String jsonString) {

        District pc = null;
        try {
            pc = mapper.readValue(jsonString, District.class);
        } catch (IOException e) {
            Logger.getLogger(District.class.getName()).log(Level.SEVERE, "Failed to parse district json string", e);
        }

        return pc;
    }
}
