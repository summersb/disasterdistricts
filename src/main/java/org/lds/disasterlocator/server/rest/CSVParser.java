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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bert Summers
 */
public class CSVParser {

    private final String data;
    private List<String> items = new ArrayList<String>();
    private int index;

    public CSVParser(String data) {
        this.data = data;
        parse();
    }

    private void parse() {
        char[] toCharArray = data.toCharArray();
        int pos = 0;
        int lastPos = 0;
        boolean inQuote = false;
        for (; pos < toCharArray.length; pos++) {
            if (toCharArray[pos] == '"' && inQuote) {
                items.add(data.substring(lastPos, pos));
                lastPos = pos + 2;
                pos++;
                inQuote = false;
            } else if (toCharArray[pos] == '"') {
                inQuote = true;
                lastPos++;
            } else if (toCharArray[pos] == ',' && !inQuote) {
                if (lastPos == pos) {
                    items.add("");
                } else {
                    items.add(data.substring(lastPos, pos));
                }
                lastPos = pos + 1;
            }
        }
    }
    

    public String next() {
        return items.get(index++);
    }

    public int count() {
        return items.size();
    }
}
