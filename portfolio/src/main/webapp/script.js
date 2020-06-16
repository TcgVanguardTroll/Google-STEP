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


const generateNavbar = () => {
    const div = document.createElement('div');
    div.className = 'row';
    div.innerHTML = `
    <a href = "./index.html"> Home </a>
    <a href = "./about.html"> About Me </a>
    <a href = "./experience.html"> Experience </a>
    <a href = "./russian.html"> Learn Russian </a>
    <a href = "./comments.html"> Comments </a>
    `;
    document.getElementById('jordanNav').appendChild(div);
};

// Array containing information about Jordan Grant.
const facts = [
    'I am interested in compilers and progamming languages.',
    'I have interned at Google twice !',
    'I have lived in three states: California, New York, and Virginia.!',
    'I enjoy writing and watching fantasy.',
    'I am a member of NSBE.',
    'I like Playing JRPGS like Disgaea and Persona ',
    `My favorite show is Law and Order: Special Victims Unit !`
];

// Function that returns a random Fact about Jordan Grant.
const addRandomFact = () => {

    // Store index in variable for future use.
    const idx = Math.floor(Math.random() * facts.length);

    // Get a random fact.
    const fact = facts[idx];
    // Gets Fact container element.
    const factContainer = document.getElementById('fact-container');
    // Trcaks the item in array if 0 end.
    if (facts.length === 0) {
        factContainer.innerText = "Thats all the facts about me !";
    } else {
        // Pop element via index.
        facts.splice(idx, 1);
        // Add it to the page.
        factContainer.innerText = fact;
    }
};

const getMessages = () => {
    const numComments = document.getElementById("num-comments").value;
    fetch('/comments?num-comments=' + numComments).then(response => response.json()).then((comments) => {
        const dataListElement = document.getElementById('comment-container');
        dataListElement.innerHTML = '';
        comments.forEach((comment) => {
            dataListElement.appendChild(createElementForComment(comment));
        })
    });
}

const getRussianWordForFlashCard = () => {
    fetch('/russian').then(response => response.json()).then((russianWord) => {
        let frontOfFlashCard = document.getElementById("flip-card-front");
        let backOfFlashCard = document.getElementById("flip-card-back")
        frontOfFlashCard.innerHTML = russianWord.translated;
        backOfFlashCard.innerText = russianWord.word;
    });
}

/**
 * Fetches comment form the servers based on input.
 */
function setAuthMode(input) {
    fetch(input).then(response => response.json()).then((status) => {
        const form = document.getElementById('comment-form');
        form.innerHTML = status;
        getMessages();
    });
}

/**
 * Sets user mode for commenting.
 */
function getUserMode() {
    setAuthMode("/login");
}

/**
 * Sets guest mode for commenting.
 */
function getGuestMode() {
    setAuthMode("/login?guest=true");
}

/**
 * Deletes comments from the server.
 */
function deleteComments() {
    fetch('/delete-data', {method: 'POST'}).then(getMessages());
}

    /**
     * Creates an table row element,<tr><td> </td></tr>, containing text.
     */
    function createElementForComment(commentObj) {
        const trElement = document.createElement('tr');
        const thElement = document.createElement('th');
        const tdElement = document.createElement('td');

        thElement.innerText = commentObj.name;
        tdElement.innerText = '"' + commentObj.comment + '"';

        thText = thElement.outerHTML;
        tdText = tdElement.outerHTML;

        trElement.innerHTML = thText + tdText;
        return trElement;
    }
}
