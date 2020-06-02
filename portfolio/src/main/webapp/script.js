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


const russianWords = 
`и
в
не
на
я
что
быть
с
он
а
это
как
то
этот
по
к
но
они
мы
она
который
из
у
же
год
человек
нехитрый
мерзавец
юго
дистанционный
окрасить` .split(/\r?\n/);
// Function that returns russian word from russianWords.
function getRandomRussianWord() {
    const idxRange = Math.floor (Math.random() * russianWords.length);
    return russianWords[idxRange];
}
// Array containing information about Jordan Grant.
let facts = [
    `I am currently learning russian, ${getRandomRussianWord()}`, 
    'I am interested in compilers and progamming languages.',
    'I have interned at Google twice !',
    'I have lived in three states: California, New York, and Virginia.!',
    'I enjoy writing and watching fantasy.',
    'I am a member of NSBE.',
    'I like Playing JRPGS like Disgaea and Persona ', 
    `My favorite show is Law and Order: Special Victims Unit !`
];
// function that returns a random Fact about Jordan Grant.
function addRandomFact(){

    // Store index in variable for future use.
    const idx = Math.floor(Math.random() * facts.length);

    // Get a random fact.
    const fact = facts[idx];
    // Gets Fact container element.
    const factContainer = document.getElementById('fact-container');
    // Trcaks the item in array if 0 end.
    if(facts.length == 0){
        factContainer.innerText = "Thats all the facts about me !";
    } else {
        // Pop element via index. 
        facts.splice(idx,1);
        // Add it to the page.
        factContainer.innerText = fact;
    }
}

function introduce(){
      fetch('/data').then(response => response.text()).then((fact) => {
    document.getElementById('fact-container').innerText = fact;
  })};

function fetchData(){
      fetch('/data').then(response => response.json()).then((data) => {
    // stats is an object, not a string, so we have to
    // reference its fields to create HTML content

    const statsListElement = document.getElementById('fact-container');
    statsListElement.innerHTML = '';
    statsListElement.appendChild(
        createListElement('Comment 1: ' + data[0]));
    statsListElement.appendChild(
        createListElement('Comment 2: ' + data[1]));
    statsListElement.appendChild(
        createListElement('Comment 3: ' + data[2]));
});
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

