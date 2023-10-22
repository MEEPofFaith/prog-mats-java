![Logo](images/ProgMin.png)

[![Commit Compiling](https://github.com/MEEPofFaith/prog-mats-java/workflows/Commit%20Test/badge.svg)](https://github.com/MEEPofFaith/prog-mats-java/actions/workflows/Commit.yml)
[![Discord](https://img.shields.io/discord/704355237246402721.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=ffd37f&label=Mindustry)](https://discord.com/invite/mindustry)
[![Stars](https://img.shields.io/github/stars/MEEPofFaith/prog-mats-java?label=Star%20the%20mod%20here%21&style=social)]()

Progression Ministry is a combination of three of my mods originally made in v5:
- Ohno Missiles
- Extra Sand in the Sandbox
- Progressed Materials

Most of the content in this mod is just experimental turrets. They probably aren't balanced, and are just me trying out various ideas.

[![Download](https://img.shields.io/github/v/release/MEEPofFaith/prog-mats-java?color=gold&include_prereleases&label=DOWNLOAD%20LATEST%20RELEASE&logo=github&logoColor=FCC21B&style=for-the-badge)](https://github.com/MEEPofFaith/prog-mats-java/releases)

_Or just find it in the in-game mod browser!_

This repo is a continuation of [this](https://github.com/MEEPofFaith/prog-mats-java-sonnicon).

_Anuke Template > Sonnicon Template_

---
## Building for Desktop Testing

1. Install JDK **17**.
2. Run `gradlew jar` [1].
3. Your mod jar will be in the `build/libs` directory. **Only use this version for testing on desktop. It will not work with Android.**
To build an Android-compatible version, you need the Android SDK. You can either let Github Actions handle this, or set it up yourself. See steps below.
4. Running `gradlew moveDesktop` can move the file to the mods folder for you. (Note: It is not properly set up for Mac. If you use a Mac, PR in the proper command for it.)
    - Run `gradlew jarMove` to build and move.

## Building through Github Actions

This repository is set up with Github Actions CI to automatically build the mod for you every commit. This requires a Github repository, for obvious reasons.
To get a jar file that works for every platform, do the following:
1. Make a Github repository with your mod name, and upload the contents of this repo to it. Perform any modifications necessary, then commit and push. 
2. Check the "Actions" tab on your repository page. Select the most recent commit in the list. If it completed successfully, there should be a download link under the "Artifacts" section. 
3. Click the download link (should be the name of your repo). This will download a **zipped jar** - **not** the jar file itself [2]! Unzip this file and import the jar contained within in Mindustry. This version should work both on Android and Desktop.

## Building Locally

Building locally takes more time to set up, but shouldn't be a problem if you've done Android development before.
1. Download the Android SDK, unzip it and set the `ANDROID_HOME` environment variable to its location.
2. Make sure you have API level 30 installed, as well as any recent version of build tools (e.g. 30.0.1)
3. Add a build-tools folder to your PATH. For example, if you have `30.0.1` installed, that would be `$ANDROID_HOME/build-tools/30.0.1`.
4. Run `gradlew deploy`. If you did everything correctlly, this will create a jar file in the `build/libs` directory that can be run on both Android and desktop.

*[1]* *On Linux/Mac it's `./gradlew`, but if you're using Linux I assume you know how to run executables properly anyway.*  
*[2]: Yes, I know this is stupid. It's a Github UI limitation - while the jar itself is uploaded unzipped, there is currently no way to download it as a single file.*

