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

package com.google.sps.data;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;

/**
 * A Public class representing a word.
 */
public class Word {

    private final String translated;
    private final String word;


    /**
     * @param word           The Word itself.
     * @param targetLanguage The language that the word is in.
     */
    public Word(String word, String targetLanguage) {
        this.word = word;
        this.translated = translateWord(word, targetLanguage);
    }

    private String translateWord(String word, String targetLanguage) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation = translate.translate(word, TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage(targetLanguage));
        return translation.getTranslatedText();
    }

    private String getTranslated() {
        return translated;
    }
}