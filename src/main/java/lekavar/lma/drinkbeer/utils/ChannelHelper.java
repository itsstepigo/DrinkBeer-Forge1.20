package lekavar.lma.drinkbeer.utils;

import lekavar.lma.drinkbeer.registries.NetworkingRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ChannelHelper {
    public static final SimpleChannel DEFAULT_CHANNEL = NetworkingRegistry.MAIN;

    public static <MSG extends Message> void RegisterMessage(int id, Class<MSG> msgClass, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        RegisterMessage(DEFAULT_CHANNEL, id, msgClass, messageConsumer, Optional.empty());
    }

    public static <MSG extends Message> void RegisterMessage(SimpleChannel channel, int id, Class<MSG> msgClass, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        RegisterMessage(channel, id, msgClass, messageConsumer, Optional.empty());
    }

    public static <MSG extends Message> void RegisterMessage(SimpleChannel channel, int id, Class<MSG> msgClass, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer, final Optional<NetworkDirection> networkDirection) {
        channel.registerMessage(
                id,
                msgClass,
                Message::toBytes,
                (FriendlyByteBuf packetBuffer) -> {
                    try {
                        MSG msg = msgClass.getDeclaredConstructor().newInstance();
                        msg.fromBytes(packetBuffer);
                        return msg;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                },
                messageConsumer,
                networkDirection);
    }
}
