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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bert W Summers
 */
public class Row {
 private List<Element> elements;

    /**
     * @return the elements
     */
    public List<Element> getElements() {
        if(elements == null){
            elements = new ArrayList<>();
        }
        return elements;
    }

    /**
     * @param elements the elements to set
     */
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"elements\":[");
        for (Element element : elements) {
            sb.append(element.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("] }\n");
        return sb.toString();
    }


}
