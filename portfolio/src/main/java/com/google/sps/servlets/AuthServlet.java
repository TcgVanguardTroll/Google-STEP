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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.data.Visitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that returns the login status of the user.
 */

@WebServlet("/login")
public class AuthServlet extends HttpServlet {

    static final String COMMENT_FORM_FILE_STRING = "/files/Comment.txt";
    static final String USER_GREETING_FILE_STRING = "/files/Greeting.txt";
    static final String GUEST_GREETING_FILE_STRING = "/files/Guest.txt";
    static final String LOGIN_OPTIONS_FILE_STRING = "/files/Login.txt";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


        // Sets the content type of the response being sent to the client as text/html.
        response.setContentType("text/html");

        // Returns a PrintWriter object that can send character text to the client.
        PrintWriter out = response.getWriter();

        // Retrieves the form parameter "Guest".
        String guest = request.getParameter("guest");

        // Gets reference to a UserService
        UserService userService = UserServiceFactory.getUserService();

        // String representing a greeting for user
        String greeting = "";

        // String representing a user's email.
        String userEmail = "N/A";

        // String representing the comment form.
        String commentForm = "";

        // Generates greeting for guests
        if (guest != null && guest.equals("true")){
            String loginUrl = userService.createLoginURL("/comments.html");
            try {
                greeting = getGuestGreeting(loginUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Generates greeting for users
        else if(userService.isUserLoggedIn()){
            userEmail = userService.getCurrentUser().getEmail();
            String logoutUrl = userService.createLogoutURL("/comments.html");
            try {
                greeting = getUserGreeting(userEmail, logoutUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Shows login options, if user is not logged in
        else {
            String loginUrl = userService.createLoginURL("/comments.html");
            String loginOptions = "";
            try {
                loginOptions = getLoginOptions(loginUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Gson gson = new Gson();
            out.println(gson.toJson(loginOptions));
            return;
        }

        // Generates comment form
        try {
            commentForm = createForm(greeting, userEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        response.getWriter().println(gson.toJson(commentForm));
    }

    /** Returns a the comment form, along with the specified greeting */
    private String createForm(String greeting, String userEmail) throws Exception{
        String commentFormHtml = new String(Files.readAllBytes(Paths.get(getClass().getResource(COMMENT_FORM_FILE_STRING).getFile())));
        commentFormHtml = String.format(commentFormHtml, greeting, userEmail);
        return commentFormHtml;
    }

    /** Returns a user greeting string */
    private String getUserGreeting(String userEmail, String logoutUrl) throws Exception{
        String greetingTemplate = new String(Files.readAllBytes(Paths.get(getClass().getResource(USER_GREETING_FILE_STRING).getFile())));
        return String.format(greetingTemplate, userEmail, logoutUrl);
    }

    /** Returns a guest greeting string */
    private String getGuestGreeting(String loginUrl) throws Exception{
        String greetingTemplate = new String(Files.readAllBytes(Paths.get(getClass().getResource(GUEST_GREETING_FILE_STRING).getFile())));
        return String.format(greetingTemplate, loginUrl);
    }

    /** Returns login options string */
    private String getLoginOptions(String loginUrl) throws Exception{
        String greetingTemplate = new String(Files.readAllBytes(Paths.get(getClass().getResource(LOGIN_OPTIONS_FILE_STRING).getFile())));
        return String.format(greetingTemplate, loginUrl);
    }
}


