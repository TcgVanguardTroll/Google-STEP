// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes all the comments within a DataStore. */
@WebServlet("/delete-data")
public class DeletionServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
         // Create Datastore .
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Create Query instance for Comment.
        Query query = new Query("Comment");

        // Contains  Comments in the Datastore 
        PreparedQuery results = datastore.prepare(query);

        // Iterate through each entity in the Datastore and delete it
        for (Entity entity : results.asIterable()) {
            datastore.delete(entity.getKey());
        }

        // Return empty response
        response.getWriter().println();

        // Redirect back to the HTML page.
        response.sendRedirect("/comments.html");  
  }
}