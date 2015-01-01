# LookingGlass

LookingGlass is an API mod targeted at providing tools and systems for mods to render locations in other dimensions.

Since the Minecraft Client has no concept of other dimensions, LookingGlass gives it one and adds systems for the server to send information to clients about worlds other than the one the client is currently in.

## Usage Information
It is advised that you work from a released API jar, rather than from the source code provided here.  This will help avoid conflicts in Minecraft instances.

### Getting the API
You can manually obtain an API jar from the LookingGlass CurseForge page: http://minecraft.curseforge.com/mc-mods/230541-lookingglass

Once equipped with your API version of choice (the most recent one, right?), you can put it in the "jars" folder at the same level as your gradle scripts.  You may need to create this folder.
You can then either import the jar manually into your workspace or rerun the gradle command to build your workspace of choice:
ex: gradlew.bat eclipse

>A maven repo is coming soon.

### Getting Started
LookingGlass uses a very robust but rather complex method of maintaining its API versions.  This allows it to support older versions of the API alongside new versions, but there is a small cost of complexity.
It is recommended you look at the APIInstanceProvider class.  It should be well documented in its usage and how to get it.  Once you have your instance of this class you are ready to get into LookingGlass's API properly.

### Available API Interfaces
>It is also possible to get a list of interfaces and their available versions supported from the APIInstanceprovider

Currently LookingGlass only has one API interface, the "view" API at version 1.
Request your copy of this API from the instance provider using the identifier "view-1".
