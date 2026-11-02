<img src="common/src/main/resources/sap_banner.png" alt="'Simple Audio Player'">

# SimpleAudioPlayer (client side)
## A library mod for Minecraft that lets you easily play audio files in the game world, "inside" player head or in the menus

# Usage:
```java 
    // the minimum code required to play sound
    SAP.play("path/to/file.mp3", AudioType.FILE);
    
    // variant with volume
    SAP.play("modid:path/to/file.wav", AudioType.LOCATION, 0.5F);
```

```java
    // to change sound volume from Minecraft sound settings screen
    // This example will use the MASTER and BLOCKS volume sliders
    SAP.play("https://example.com/path/to/audio.ogg", AudioType.URL, SoundSource.BLOCKS);

    // variant with volume
    SAP.play("https://example.com/path/to/audio.ogg", AudioType.URL, SoundSource.BLOCKS, 0.5F);
```

```java
    // All settings can be applied in this way
    SAP.play("path/to/file.mp3", AudioType.FILE, instance -> {
        instance.setLooping(true);
        instance.setReferenceDistance(4);
        instance.setMaxDistance(15);
        instance.setVolume(0.25F);
        // ...
    });
```

## Positional

```java
    // Play sound with 0.5 volume in world on position x10 y50 z10
    SAP.playPositional("path/to/file.mp3", AudioType.FILE, 0.5F, 10, 50, 10);

    // same
    SAP.playPositional("path/to/file.mp3", AudioType.FILE, new Vector3f(10, 50, 10), 0.5F);
```

```java
    // All settings can be applied in this way
    SAP.playPositional("modid:path/to/file.ogg", AudioType.LOCATION, instance -> {
        instance.setPosition(new Vec3(512, 42, 993));
        instance.setVolume(0.75f);
        // ...
    });
```

## More control
```java
    // Creates sound instance of resource, boolean - positional (true/false).
    // You can store sound instance and use it later
    // play, pause, stop, restart, set volume, position, ...
    // use instance.close() if instance is no longer needed
    SAP.create("https://example.com/path/to/audio.mp3", AudioType.URL, false)
        .thenAccept(soundInstance -> {
            soundInstance.setSoundChannel(SoundSource.AMBIENT);
            soundInstance.setVolume(0.25F);
            soundInstance.play();
        }).exceptionallyAsync(throwable -> {
            Constants.LOG.error("Failed to create sound instance: ", throwable);
            return null;
        });

```

```java
    // checks whether openAL ready. Returns boolean
    SAP.isReadyForPlaying();

    // the code inside will be executed automatically
    // after OpenAL has loaded
    SAP.runOnSoundEngineLoaded(() -> {
        //... f.e. create sound    
    });
    // you don't need to place SAP.play() methods inside this block
    // they already use it
```

## See more in [`SAP`](https://github.com/Vaneck512/SimpleAudioPlayer/blob/main/common/src/main/java/ru/vaneck/sap/lib/SAP.java) class

### License
SimpleAudioPlayer is licensed under MIT
