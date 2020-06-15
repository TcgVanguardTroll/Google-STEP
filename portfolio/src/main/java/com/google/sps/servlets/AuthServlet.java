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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that returns the login status of the user.
 */

@WebServlet("/login")
public class AuthServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Gets reference to a UserService
        UserService userService = UserServiceFactory.getUserService();

        // Checks status of user login and creates corresponding URL
        boolean loggedIn = userService.isUserLoggedIn();
        String url = loggedIn ? userService.createLogoutURL("/") : userService.createLoginURL("/");

        // Create login object based on current login status and login url
        Visitor visitor = new Visitor(loggedIn, url);

        // Create JSON response string using Gson and send JSON response
        String json = new Gson().toJson(visitor);
        response.setContentType("application/json");
        response.getWriter().println(json);
    }
}

