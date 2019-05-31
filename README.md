jmIrc
=====

IRC client for all MIDP-1.0 compatible mobile phones and other devices.

Setting up dev environment on macOS (10.14 Mojave)
--------------------------------------------------

Download and install the legacy Java 6 runtime from
[Apple](https://support.apple.com/kb/DL1572) to be able to run the Java ME SDK
3.0.

Download and install the Java ME SDK 3.0 from
[Oracle](https://www.oracle.com/technetwork/java/embedded/javame/javame-sdk/downloads/java-me-sdk-3-0-1849684.html).

Download [ProGuard](https://sourceforge.net/projects/proguard/) version 4.11,
because the 5.x and later are not compatible with the SDK.

Copy proguard4.11/lib/proguard.jar library to
/Applications/Java_ME_SDK_3.0.app/Contents/Resources/javamesdk/mobility8/external/proguard/
to be able to do obfuscated and minified builds.

Start the SDK with explicitly defining the used Java version, it will crash if
you try to run it with e.g. Java 8:

```
/Applications/Java_ME_SDK_3.0.app/Contents/MacOS/javamesdk --jdkhome /Library/Java/JaVirtualMachines/1.6.0.jdk/Contents/Home
```

Open the project, run build and the resulting jmIrc.jad and jmIrc.jad files
should appear in the dist/ directory under the project.

If you want to test the application in the emulator, you can click the "Run Main
Project via OTA", which will install jmIrc to the emulator and then run it. If
you click the normal play button it will also work, but all attempts to connect
to the network will fail because of a failure in permissions.

Please notice that the Java 6 and Java ME SDK 3.0 combination is not really
compatible with the latest macOS versions, so it will crash occasionally with an
error from the AWT JNI library. You probably just have to bear with that, after
all this is 15 year old and mostly deprecated technology...

