# Scribe Reader

## What is this?
This is an app I'm making to help advanced language learners consume native Japanese books and content. 
This takes heavy inspiration from Duolingo with it's gamified quizzes throughout the reading.

### Core Features
- [x] On device parsing of .epub file
- [x] On device tokenization of Japanese text
- [x] On device dictionary using JMDict (custom dictionary support planned no eta)
- [x] Searchable dictionary from anywhere in the app with sentence tokenization and custom input
- [x] Basic TTS to read out the story
- [ ] SRS/Flash card system
- [ ] Add cards to review when definition/reading is viewed during story
- [ ] Song Lyric Retrieval and analysis
- [ ] Anime/Video Subtitle processing/retrieval
- [ ] Website retrieval, processing and tokenization  
      
# Start of Chapter 1 of a certain light novel
![Image Display](./res/Screenshot_image_display.jpg)

# Example of UI element for viewing the definition of a word with ambiguity
![Entry with multiple definitions](./res/Screenshot_multiple_definition_display.jpg)

# Example of UI Element for viewing the definition of a word without ambiguity
![Entry with single definition](./res/Screenshot_single_definition_display.jpg)

# Example of UI Element for viewing the reading, dictionary form, and part of speech of a word
![Kana Reading](./res/Screenshot_reading_display.jpg)

## As of initial commit to this repository, epubs are no longer processed in another application and retrieved by the app. Now all is done in the app.

## Attributions
This application uses [JMDict](http://edrdg.org/jmdict/j_jmdict.html)

### This repository contains a few other slightly altered repositories because I couldn't figure out how to add them through gradle
- [TokenizerMobileWrapper](https://github.com/DeveloperTruthStare/TokenizerMobileWrapper) / [Kogame](https://github.com/ikawaha/kagome/tree/v2/tokenizer) - for tokenization of Japanese text
- [Epublib](https://github.com/psiegman/epublib/tree/master/epublib-core/src/main/java/nl/siegmann/epublib) - for parsing .epub files
- [Jazzlib](https://github.com/psiegman/epublib/tree/master/epublib-core/src/main/java/net/sf/jazzlib) - used by Epublib for unzipping files
