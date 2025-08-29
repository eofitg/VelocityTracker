package net.eofitg.velocitytracker.commands;

import net.eofitg.velocitytracker.VelocityTracker;
import net.eofitg.velocitytracker.config.DefaultConfig;
import net.eofitg.velocitytracker.hud.VelocityOverlayRenderer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandVelocityTracker extends CommandBase {
    @Override
    public String getCommandName() {
        return "velocitytracker";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/velocitytracker toggle|scale <value>|alpha <value>|graphsize <value>|posx <value>|posy <value>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("§eUsage: " + getCommandUsage(sender)));
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "toggle": {
                VelocityOverlayRenderer.enabled = !VelocityOverlayRenderer.enabled;
                sender.addChatMessage(new ChatComponentText("§aBlock overlay enabled: " + VelocityOverlayRenderer.enabled));
                VelocityTracker.configHandler.save();
                break;
            }
            case "scale": {
                if (args.length >= 2) {
                    try {
                        VelocityOverlayRenderer.scale = Float.parseFloat(args[1]);
                        sender.addChatMessage(new ChatComponentText("§a[VT] Scale: " + VelocityOverlayRenderer.scale));
                        VelocityTracker.configHandler.save();
                    } catch (NumberFormatException e) {
                        sender.addChatMessage(new ChatComponentText("§cPlease enter a valid number!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§e[VT] Current scale: " + VelocityOverlayRenderer.scale));
                }
                break;
            }
            case "alpha": {
                if (args.length >= 2) {
                    try {
                        VelocityOverlayRenderer.alpha = Integer.parseInt(args[1]);
                        sender.addChatMessage(new ChatComponentText("§a[VT] Alpha: " + VelocityOverlayRenderer.alpha));
                        VelocityTracker.configHandler.save();
                    } catch (NumberFormatException e) {
                        sender.addChatMessage(new ChatComponentText("§cPlease enter a valid number!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§e[VT] Current alpha: " + VelocityOverlayRenderer.alpha));
                }
                break;
            }
            case "graphsize": {
                if (args.length >= 2) {
                    try {
                        VelocityOverlayRenderer.graphSize = Integer.parseInt(args[1]);
                        sender.addChatMessage(new ChatComponentText("§a[VT] Graph size: " + VelocityOverlayRenderer.graphSize));
                        VelocityTracker.configHandler.save();
                    } catch (NumberFormatException e) {
                        sender.addChatMessage(new ChatComponentText("§cPlease enter a valid number!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§e[VT] Current graph size: " + VelocityOverlayRenderer.graphSize));
                }
                break;
            }
            case "posx": {
                if (args.length >= 2) {
                    try {
                        VelocityOverlayRenderer.posX = Integer.parseInt(args[1]);
                        sender.addChatMessage(new ChatComponentText("§aX position: " + VelocityOverlayRenderer.posX));
                        VelocityTracker.configHandler.save();
                    } catch (NumberFormatException e) {
                        sender.addChatMessage(new ChatComponentText("§cPlease enter a valid number!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§eCurrent X position: " + VelocityOverlayRenderer.posX));
                }
                break;
            }
            case "posy": {
                if (args.length >= 2) {
                    try {
                        VelocityOverlayRenderer.posY = Integer.parseInt(args[1]);
                        sender.addChatMessage(new ChatComponentText("§aY position: " + VelocityOverlayRenderer.posY));
                        VelocityTracker.configHandler.save();
                    } catch (NumberFormatException e) {
                        sender.addChatMessage(new ChatComponentText("§cPlease enter a valid number!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§eCurrent Y position: " + VelocityOverlayRenderer.posY));
                }
                break;
            }
            case "vyrange": {
                if (args.length >= 2) {
                    try {
                        VelocityOverlayRenderer.vyRange = Float.parseFloat(args[1]);
                        sender.addChatMessage(new ChatComponentText("§aV/Y range: " + VelocityOverlayRenderer.vyRange));
                        VelocityTracker.configHandler.save();
                    } catch (NumberFormatException e) {
                        sender.addChatMessage(new ChatComponentText("§cPlease enter a valid number!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§eCurrent V/Y range: " + VelocityOverlayRenderer.vyRange));
                }
                break;
            }
            default: {
                sender.addChatMessage(new ChatComponentText("§cUnknown argument: " + sub));
                sender.addChatMessage(new ChatComponentText("§eUsage: " + getCommandUsage(sender)));
            }
        }
    }



    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "toggle", "scale", "alpha", "graphsize",
            "posx", "posy", "vyrange"
    );

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (String cmd : SUBCOMMANDS) {
                if (cmd.startsWith(prefix)) {
                    completions.add(cmd);
                }
            }
        }
        else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "scale": {
                    completions.add(DefaultConfig.VelocityOverlay.scale + "");
                    break;
                }
                case "alpha": {
                    completions.add(DefaultConfig.VelocityOverlay.alpha + "");
                    break;
                }
                case "graphsize": {
                    completions.add(DefaultConfig.VelocityOverlay.graphSize + "");
                    break;
                }
                case "posx": {
                    completions.add(DefaultConfig.VelocityOverlay.posX + "");
                    break;
                }
                case "posy": {
                    completions.add(DefaultConfig.VelocityOverlay.posY + "");
                    break;
                }
                case "vyrange": {
                    completions.add(DefaultConfig.VelocityOverlay.vyRange + "");
                    break;
                }
            }
        }

        return completions.isEmpty() ? null : completions;
    }
}
