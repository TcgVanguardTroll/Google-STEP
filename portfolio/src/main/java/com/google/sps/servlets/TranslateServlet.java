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

import java.lang.System;

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

    // Data-Structure for storing words in russian.
    List<String> untranslated;

     @Override
    public void init() {
        try {
            getRussianWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param req  Http Servlet request.
     * @param resp Response from Http Servlet request.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        // Fetches a random Russian Word generated from text file.
        String russianWord = getRandomRussianWord();

        // Helper Function that generates new russian word class from the fetched russian word in array.
        Word wordToSend = new Word(russianWord, "en");

        // Prints russian word to the console.
        try {
            // Alerts client to expect application/json .
            resp.setCharacterEncoding("UTF8");
            resp.setContentType("application/json;");
            resp.getWriter().println(convertWordToJson(wordToSend));
        } catch (IOException e) {
            System.out.println("You Messed Up");
            e.printStackTrace();
        }
    }

    private void getRussianWords() throws IOException {
        // Data Structure to store the words.
        untranslated = new ArrayList<>();
        // File reader used to get the foreign language vocabulary.
        BufferedReader fileToBeRead = new BufferedReader(new FileReader(new File("./files/ru.txt")));
        // String representing the word to be translated.
        String russianWord;
        // While line is available add words to array.
        while ((russianWord = fileToBeRead.readLine()) != null) {
            untranslated.add(russianWord);
        }
    }

    private String convertWordToJson(Word word){
            Gson gson = new Gson();
            String json = gson.toJson(word);
            return json;
    }

    // Function that returns russian word from russianWords.
    private String getRandomRussianWord() {
        int idxRange = (int) Math.floor(Math.random() * untranslated.size());
        return untranslated.get(idxRange);
    }

}
