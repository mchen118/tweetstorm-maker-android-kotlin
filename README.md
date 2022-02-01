# Tweetstorm Maker Android Kotlin
This is intended to be a newer version of Tweetstorm Maker on [Google Play Store](https://play.google.com/store/apps/details?id=com.muchen.tweetstormandroid.release), and [the original version written in Java](https://github.com/mchen118/tweetstorm-maker-android-java) will not be updated anymore.

## [Privacy Statement](https://github.com/mchen118/tweetstorm-maker-android-kotlin/blob/master/GOOGLE_PLAY_PRIVACY_STATEMENT.md)

## Overview
This new version was written in Kotlin from scratch, and the differences with the old Java version are plenty:

1. It adds a new feature to allow users to recall (delete from Twitter's server) a tweetstorm that was sent using this app with one click, just like the user was able to send a tweetstorm with one click.
2. It has unit, integration and UI tests.
3. It follows the Clean Architecture *for the most part*.
4. It uses MVVP instead of MVP UI pattern.
5. It uses Kotlin's Coroutine to manage asynchronous operations, instead of Java Executors.
6. It relies on Android Jetpack's Navigation library to handle fragment navigation and parts of the UI.
...

Overall the code base has become much larger, in exchange for much better scalability and modularity. I hope it is also much less prone to defects.