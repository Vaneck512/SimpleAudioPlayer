package ru.vaneck.sap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import ru.vaneck.sap.lib.sound.AudioFactory;
import ru.vaneck.sap.lib.sound.AudioType;
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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("play").executes(context -> {
                AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
                AudioFactory.createSound("C:\\users\\ivan\\downloads\\Tryhardninja-Follow-Me.ogg", AudioType.FILE)
                        .thenAccept(soundInstance -> {
                            System.out.println(123);
                            soundInstance.setGain(0.35F);
                            soundInstance.setPosition(new Vector3f(0, -60, 0));
                            soundInstance.play();
                            System.out.println(123);
                        });
                return 1;
            }));
//            dispatcher.register(ClientCommands.literal("play").executes(context -> {
//                if (source[0] != null) source[0].stop();
//                AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
//                SoundBuffer buffer = new SoundBuffer("C:\\users\\ivan\\downloads\\Tryhardninja-Follow-Me.ogg");
//                source[0] = new SoundSource(true, false);
//                source[0].setBuffer(buffer.getBufferId());
//                source[0].setPosition(new Vector3f(0, -60, 0));
//                source[0].setGain(0.25F);
//                source[0].play();
//                return 1;
//            }));
//            dispatcher.register(ClientCommands.literal("play").executes(context -> {
//                try (DecodedAudio decoded = OggDecoder.decode(Path.of("C:\\users\\ivan\\downloads\\Tryhardninja-Follow-Me.ogg"))) {
//
//                     ALBuffer buffer = new ALBuffer(decoded.pcm(), decoded.format());
//                     ALInstance instance = new ALInstance(buffer) ;
//
//                    instance.setPosition(2f, -60f, -3f);
//                    instance.setGain(0.25f);
//                    instance.setLooping(false);
//                    instance.play();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                return 1;
//            }));
        });
    }
}
