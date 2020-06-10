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
 import com.google.appengine.api.datastore.FetchOptions;
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
/**
 * Servlet that returns some example content.
 */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

    // Data Structure for storing Comments.
    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Creating a Comment Query .
        Query query = new Query("Comment");
        //
        int numOfComments = getNumOfComments(request);

        if (numOfComments < 1 || numOfComments > 99) {
            numOfComments = -1;
        }

        // Creating a PreparedQuery storing results.
        PreparedQuery results = datastore.prepare(query);
        // Storing comment querys within Array.

        List<Comment> comments = new ArrayList<>();
        Gson gson = new Gson();

        if (numOfComments != -1) {
            List<Entity> listResults = results.asList(FetchOptions.Builder.withLimit(numOfComments)) ;
            for (Entity entity : listResults) {
                    String name = (String) entity.getProperty("name");
                    String pageComment = (String) entity.getProperty("comment");
                    Comment comment = new Comment(name, pageComment);
                    comments.add(comment);
                }
        }
        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the input from the form.
        String comment = getComment(request);

        // Get the name from the form.
        String name = getName(request);


        // Create an entity representing the users comment.
        Entity commentEntity = new Entity("Comment");

        // Ensuring whether or not the user's comment and name werent null.
        if (comment != null && name != null) {
            commentEntity.setProperty("name", name);
            commentEntity.setProperty("comment", comment);
            // Putting the comment entity within the datastore.
            datastore.put(commentEntity);
        }

        // Redirect back to the HTML page.
        response.sendRedirect("/comments.html");
    }

    /**
     * @return the request parameter, or the default value if the parameter
     * was not specified by the client
     */
    private String getComment(HttpServletRequest request) {
        //   Get comment from form.
        return request.getParameter("comment");
    }

    private String getName(HttpServletRequest request) {
        //   Get name from form.
        return request.getParameter("name");
    }

    /**
     * @return -1 in the case of malformed input, if not the function will return the
     * int equaivlent of the ammount of comments within the string.
     */
    private int getNumOfComments(HttpServletRequest request) {
        String numOfCommentsString = request.getParameter("num-comments");
        if (numOfCommentsString == null) {
            return -1;
        }
        int comments;
        try {
            comments = Integer.parseInt(numOfCommentsString);
            return comments;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}