package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class ExampleMod implements DedicatedServerModInitializer {

    private static boolean netherEnabled = false;
    private static boolean endEnabled = false;

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (destination.getRegistryKey() == World.NETHER && !netherEnabled) {
                ServerWorld overworld = player.getServer().getWorld(World.OVERWORLD);
                if (overworld != null) {
                    player.teleport(overworld, 0.5, 100.0, 0.5, player.getYaw(), player.getPitch());
                    player.sendMessage(Text.literal("§cDer Nether ist aktuell gesperrt!"), false);
                }
            }

            if (destination.getRegistryKey() == World.END && !endEnabled) {
                ServerWorld overworld = player.getServer().getWorld(World.OVERWORLD);
                if (overworld != null) {
                    player.teleport(overworld, 0.5, 100.0, 0.5, player.getYaw(), player.getPitch());
                    player.sendMessage(Text.literal("§cDas End ist aktuell gesperrt!"), false);
                }
            }
        });
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("dimension")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("nether")
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> {
                        netherEnabled = BoolArgumentType.getBool(context, "enabled");
                        String status = netherEnabled ? "§afreigegeben" : "§cgesperrt";
                        context.getSource().sendFeedback(() -> Text.literal("Der Nether wurde " + status + "!"), true);
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("end")
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> {
                        endEnabled = BoolArgumentType.getBool(context, "enabled");
                        String status = endEnabled ? "§afreigegeben" : "§cgesperrt";
                        context.getSource().sendFeedback(() -> Text.literal("Das End wurde " + status + "!"), true);
                        return 1;
                    })
                )
            )
        );
    }
}
