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

import com.google.appengine.api.datastore.*;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.sps.data.Word;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet utilized for the translation app.
 */
@WebServlet("/russian")
public class TranslateServlet extends HttpServlet {

    // Data Store for Words.
    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Data-Structure for storing words in russian.
    List<String> untranslated;

    @Override
    public void init() throws ServletException {
        try {
            getRussianWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    /**
     * @param req  Http Servlet request.
     * @param resp Response from Http Servlet request.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        // Creating a Word Query .
        Query query = new Query("Word");

        Gson gson = new Gson();

        // Fetches a random Russian Word generated from text file.
        String russianWord = getRandomRussianWord();

        // Helper Function that generates new russian word class from the fetched russian word in array.
        Word wordToSend = new Word(russianWord, "ru");

        String wordToSendAsJson = gson.toJson(wordToSend);

        // Alerts client to expect application/json .
        resp.setContentType("application/json;");

        // Prints russian word to the console.
        try {
            resp.getWriter().println(wordToSendAsJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRussianWords() throws IOException {
        // Data Structure to store the words.
        untranslated = new ArrayList<>();
        // File reader used to get the foreign language vocabulary.
        BufferedReader fileToBeRead = new BufferedReader(new FileReader(new File("/files/ru.txt")));
        // String representing the word to be translated.
        String russianWord;
        // While line is available add words to array.
        while ((russianWord = fileToBeRead.readLine()) != null) {
            untranslated.add(russianWord);
        }
    }

    // Function that returns russian word from russianWords.
    private String getRandomRussianWord() {
        int idxRange = (int) Math.floor(Math.random() * untranslated.size());
        return untranslated.get(idxRange);
    }

    private int getNumberOfWords() {
        return untranslated.size();
    }
}
