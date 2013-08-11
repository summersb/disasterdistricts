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
package org.lds.disasterlocator.server.rest;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Bert W Summers
 */
public class EntityManagerFactoryHelper {

    public static EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.ConnectionURL", "jdbc:mysql://localhost:3306/disaster");
        props.put("openjpa.ConnectionDriverName", "com.mysql.jdbc.Driver");
        props.put("openjpa.ConnectionUserName", "disaster");
        props.put("openjpa.ConnectionPassword", "password");
        return Persistence.createEntityManagerFactory("disaster", props);
    }
}
