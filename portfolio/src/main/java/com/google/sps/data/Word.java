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
import com.google.cloud.translate.Translation;

/** A Public class representing a word. */
public class Word {

    private final String name;
    private final String langCode;
    private final String translated;

    /**
     * @param name The Word itself.
     * @param langCode The language that the word is in.
     */
    public Word(String name, String langCode) {
        this.name = name;
        this.langCode = langCode;
        this.translated = translateWord(name,langCode);
    }

    private String translateWord(String name, String langCode){
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation = translate.translate(name, Translate.TranslateOption.targetLanguage(langCode));
        return translation.getTranslatedText();
    }

    Word test = new Word("hello","ru");
}
