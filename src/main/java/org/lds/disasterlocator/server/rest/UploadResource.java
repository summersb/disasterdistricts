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
package org.lds.disasterlocator.server.rest;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.lds.disasterlocator.shared.File;
import org.lds.disasterlocator.shared.MyAutoBeanFactory;
import org.lds.disasterlocator.shared.Row;

/**
 *
 * @author Bert Summers
 */
@Path("upload")
public class UploadResource {
    private static final Logger logger = Logger.getLogger(UploadResource.class.getName());

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm FileUploadForm form) {
        try {
            // parse data
            MyAutoBeanFactory factory = AutoBeanFactorySource.create(MyAutoBeanFactory.class);
            AutoBean<File> fileAB = factory.create(File.class);
            File file = fileAB.as();


            String data = new String(form.getData());
            BufferedReader br = new BufferedReader(new StringReader(data));
            String header = br.readLine();
            List<Row> rowList = new ArrayList<>();
            while (header != null) {
                // create row
                AutoBean<Row> rowAB = factory.create(Row.class);
                Row row = rowAB.as();
                rowList.add(row);
                // parse header, create cell
                CSVParser parsed = new CSVParser(header);
                List<String> cells = new ArrayList<>(parsed.count());
                for (int i = 0; i < parsed.count(); i++) {
                    cells.add(parsed.next());
                }
                row.setCells(cells);
                header = br.readLine();
            }
            file.setRows(rowList);
            String json = AutoBeanCodex.encode(fileAB).getPayload();
            return Response.ok().entity(json).build();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to parse file", ex);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}