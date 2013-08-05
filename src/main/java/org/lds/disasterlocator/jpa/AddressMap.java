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
package org.lds.disasterlocator.jpa;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 *
 * @author summersb
 */
@Entity(name="AddressMap")
public class AddressMap implements Serializable {
    @Id
    private String id;
    
    @Lob
    @Column(name = "urlkey", length=8192)
    private String key;
    @Lob
    @Column(name ="urlvalue", length=8192)
    private String value;

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public void setId(String hash) {
        this.id = hash;
    }
    
    public String getId(){
        return id;
    }
}
