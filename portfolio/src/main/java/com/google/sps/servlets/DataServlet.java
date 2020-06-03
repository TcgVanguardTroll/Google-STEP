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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import java.util.*;  
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private String name = "Jordan";

    private List<String> comments = new ArrayList<String>();

    @Override public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.getWriter().println((json));    
    }

      @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String comment = getComment(request);

    // Add comment to comments Array.
    comments.add(comment);

    response.setContentType("text/html;");
    response.getWriter().println(comments);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");  
    }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getComment(HttpServletRequest request) {
    //   Get comment from form.
    String comment = request.getParameter("comment");
    if (comment == null) {
      return "Welp";
    }
    return comment;
  }
}


