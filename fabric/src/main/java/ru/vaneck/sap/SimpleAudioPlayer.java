package ru.vaneck.sap;

import net.fabricmc.api.ModInitializer;
import ru.vaneck.sap.lib.sound.model.SoundInstance;

public class SimpleAudioPlayer implements ModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();

        final SoundInstance[] source = new SoundInstance[1];

//        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
//            dispatcher.register(ClientCommands.literal("play")
//                            .then(ClientCommands.argument("source", StringArgumentType.string())
//                            .then(ClientCommands.argument("type", StringArgumentType.string())
//                                    .suggests((context, builder) -> {
//                                        for (AudioType type : AudioType.values())
//                                            builder.suggest(type.toString());
//                                        return builder.buildFuture();
//                                    })
//
//                    .executes(context -> {
//                AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
////                AudioFactory.createStreamingSound("C:\\users\\ivan\\downloads\\Бобр.wav", AudioType.FILE)
////                        .thenAccept(soundInstance -> {
////                            soundInstance.setVolume(0.35F);
////                            soundInstance.setPosition(new Vector3f(0, -60, 0));
////                            soundInstance.play();
////                        });
//
//                try {
//                    String s = StringArgumentType.getString(context, "source");
//                    AudioType type = AudioType.valueOf(StringArgumentType.getString(context, "type").toUpperCase());
//                    AudioFactory.playOnce(
//                            s,
//                            type,
//                            true,
//                            false,
//                            audioInstance -> {
//                                audioInstance
//                                        .setVolume(0.35F)
//                                        .setPosition(new Vector3f(0, -58, 0))
//                                        .setLooping(true)
//                                        .maxDistance(20)
//                                        .setReferenceDistance(0);
////                                new Thread(() -> {
////                                    try {
////                                        Thread.sleep(16000);
////                                        audioInstance.stop();
////                                        Thread.sleep(1000);
////                                        audioInstance.stop();
////                                        Thread.sleep(1000);
////                                        audioInstance.close();
////                                        Thread.sleep(1000);
////                                        audioInstance.close();
////                                    } catch (Exception e) {
////                                        throw new RuntimeException(e);
////                                    }
////                                }).start();
//                            }
//                    );
//
//
////                    AudioFactory.createSound("https://www.dropbox.com/scl/fi/ai2oyuxw4c5oktk77153r/DJ-Asul-MATADORA.mp3?rlkey=cxlf8q4ciql6z6kd2hr4erlhp&st=96eo9reu&dl=1", AudioType.URL, true)
////                            .thenAccept(instance -> {
////                                instance.play();
////                                new Thread(() -> {
////                                    try {
////                                        instance.setPosition(new Vector3f(0, -58, 0));
////                                        instance.setVolume(2.5F);
////                                        instance.setVelocity(new Vector3f(10, 0, 0));
////                                        Thread.sleep(15000);
////                                        instance.close();
////                                        instance.setVolume(0.35F);
////                                        instance.pause();
////                                        Thread.sleep(5000);
////                                        instance.play();
////                                        Thread.sleep(30000);
////                                        instance.pause();
////                                        Thread.sleep(30000);
////                                        instance.play();
////                                        AlErrorHandler.checkOpenAlError();
////                                    } catch (Exception e) {
////                                        Constants.LOG.error(e.getLocalizedMessage(), e);
////                                        throw new RuntimeException(e);
////                                    }
////                                }).start();
////                            });
//
//                } catch (Exception e) {
//                    Constants.LOG.error(e.getLocalizedMessage());
//                    throw new RuntimeException(e);
//                } catch (Throwable e) {
//                    throw new RuntimeException(e);
//                }
//
//                        return 1;
//            }))));
////            dispatcher.register(ClientCommands.literal("play").executes(context -> {
////                AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
////                AudioFactory.createStreamingSound("https://ru.hitmoz.org/get/music/20260421/p3b3_-_The_Scooby_Snacks_81333032.mp3", AudioType.URL)
////                        .thenAccept(soundInstance -> {
////                            System.out.println(123);
////                            soundInstance.setGain(0.35F);
////                            soundInstance.setPosition(new Vector3f(0, -60, 0));
////                            soundInstance.play();
////                            System.out.println(123);
////                        });
////                return 1;
////            }));
////            dispatcher.register(ClientCommands.literal("play").executes(context -> {
////                if (source[0] != null) source[0].stop();
////                AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
////                SoundBuffer buffer = new SoundBuffer("C:\\users\\ivan\\downloads\\Tryhardninja-Follow-Me.ogg");
////                source[0] = new SoundSource(true, false);
////                source[0].setBuffer(buffer.getBufferId());
////                source[0].setPosition(new Vector3f(0, -60, 0));
////                source[0].setGain(0.25F);
////                source[0].play();
////                return 1;
////            }));
////            dispatcher.register(ClientCommands.literal("play").executes(context -> {
////                try (DecodedAudio decoded = OggDecoder.decode(Path.of("C:\\users\\ivan\\downloads\\Tryhardninja-Follow-Me.ogg"))) {
////
////                     ALBuffer buffer = new ALBuffer(decoded.pcm(), decoded.format());
////                     ALInstance instance = new ALInstance(buffer) ;
////
////                    instance.setPosition(2f, -60f, -3f);
////                    instance.setGain(0.25f);
////                    instance.setLooping(false);
////                    instance.play();
////                } catch (IOException e) {
////                    throw new RuntimeException(e);
////                }
////                return 1;
////            }));
//        });
    }
}
