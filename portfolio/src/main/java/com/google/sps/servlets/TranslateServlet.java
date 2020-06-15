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

import com.google.gson.Gson;

import com.google.sps.data.Word;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.System;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Servlet utilized for the translation app.
 */
@WebServlet("/russian")
public class TranslateServlet extends HttpServlet {

    // Data-Structure for storing words in russian.
    List<String> untranslated;

    Set<String> langCodes;

    /**
     * Initialization function that populates the untranslated array with strings representing words to be translated.
     */
    @Override
    public void init() {
        try {
            getWords();
            populateLanguageCodes();
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

        // Fetches a random Word generated from untranslated array.
        String russianWord = getRandomWord();

        // Helper Function that generates new russian word class from the fetched russian word in array.
        Word wordToSend = getNewWord();

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

    /**
     * Function that populates the untranslated array.
     *
     * @throws IOException
     */
    private void getWords() throws IOException {
        // Data Structure to store the words.
        untranslated = new ArrayList<>();
        // File reader used to get the foreign language vocabulary.
        BufferedReader fileToBeRead = new BufferedReader(new FileReader(new File("./files/words.txt")));
        // String representing the word to be translated.
        String russianWord;
        // While line is available add words to array.
        while ((russianWord = fileToBeRead.readLine()) != null) {
            untranslated.add(russianWord);
        }
    }

    /**
     * Generates all ISO language codes and adds them to the languageCodes set.
     */
    private void populateLanguageCodes() {
        // String array that is populated with ISO language codes.
        String[] isoLanguages = Locale.getISOLanguages();
        // Populates the langCodes set with the strings within isoLanguages string array.
        langCodes.addAll(Arrays.asList(isoLanguages));

    }

    /**
     * Factory function that returns a new word
     *
     * @return A new instance of the Word class.
     * TODO(grantjustice) Implement a way in order for me to get a language code from parameter to incorporate
     * non russian words .
     */
    private Word getNewWord() {
        String word = getRandomWord();
        String langToTranslateTo;
        if (!langCodes.contains("ru")) {
            langToTranslateTo = "en";
        } else {
            langToTranslateTo = "ru";
        }
        return new Word(word, langToTranslateTo);
    }

    /**
     * @param word
     * @return
     */
    private String convertWordToJson(Word word) {
        Gson gson = new Gson();
        String json = gson.toJson(word);
        return json;
    }

    /**
     * @return String representing a random word form within the untranslated array.
     */
    // Function that returns russian word from russianWords.
    private String getRandomWord() {
        int idxRange = (int) Math.floor(Math.random() * untranslated.size());
        return untranslated.get(idxRange);
    }

}
