# A Sokoban game for Android devices. #

## Overview ##

A Sokoban game for Android devices. This is a port - or rather a rewrite - of my  ['sokoban for midp devices'](http://code.google.com/p/mobilesokoban/) app. I did it a few months ago when I was playing around with the Android OS.

**More free stuff can be found on my site, [xomzom.com](http://www.xomzom.com)**


## How to play ##
Sokoban is a logic puzzle game. You play a warehouse keeper (the circle). You need to put the boxes in their proper place, marked in dark-gray. However, you can only push the crates from behind, and only one crate at a time.


## Installation ##
Simply get the game from the Android market. Or use this code:

![http://chart.apis.google.com/chart?cht=qr&chs=150x150&chl=market://search?q=pname:com.xomzom.androidstuff.sokoban&suffix=.png](http://chart.apis.google.com/chart?cht=qr&chs=150x150&chl=market://search?q=pname:com.xomzom.androidstuff.sokoban&suffix=.png)

Or go to the [download page](http://code.google.com/p/sokobandroid/downloads/list) and download the latest version to your device.


## Credits and License ##

The level files themselves were created by [David W Skinner](http://users.bentonrea.com/~sasquatch/sokoban/).
The game is released under the GPL licence (V3).
Most of the graphics was taken from various publicly-available sources on the web. Please see the ['CREDITS'](http://code.google.com/p/sokobandroid/source/browse/CREDITS) file for details.


## Issues ##
This game was only tested on my Nexus One (and the SDK emulator), but should, I think, work on other devices as well. I am not aware of any issues. Please report any problems you find on http://code.google.com/p/sokobandroid/issues/list, and I'll try to fix them.


## Building your own ##
The project can be built using the build.xml ant script on the root directory. You'll need the [android SDK](http://developer.android.com/sdk/index.html). A debug version can be built by typing `ant debug`. To build a release version, you'll also need to create own key, and change build.properties to point to it. See http://developer.android.com/guide/developing/other-ide.html for more information on building android projects.
Building from Eclipse probably works as well, but I haven't tried it on machines other than my own.