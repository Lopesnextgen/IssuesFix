package davi.lopes.issuesfix.command;

import davi.lopes.issuesfix.IssuesFix;
import davi.lopes.issuesfix.update.IssuesFixUpdateChecker;
import davi.lopes.issuesfix.update.IssuesFixUpdateChecker.Snapshot;
import davi.lopes.issuesfix.update.IssuesFixUpdateChecker.Status;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.net.URI;

public final class IssuesFixCommand {
    private IssuesFixCommand() {
    }

    public static void execute() {
        Minecraft client = Minecraft.getInstance();
        send(client.player, Component.literal("[IssuesFix] Checking GitHub releases...").withStyle(ChatFormatting.YELLOW));
        IssuesFixUpdateChecker.refresh(true).thenAccept(snapshot -> client.execute(() -> show(client.player, snapshot)));
    }

    private static void show(LocalPlayer player, Snapshot snapshot) {
        if (player == null) {
            return;
        }

        send(player, Component.literal("---------------- IssuesFix ----------------").withStyle(ChatFormatting.DARK_GRAY));
        send(player, Component.literal("Status: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(status(snapshot)).withStyle(snapshot.status() == Status.WORKING ? ChatFormatting.GREEN : ChatFormatting.RED)));
        send(player, Component.literal("Version: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(IssuesFix.MOD_VERSION).withStyle(ChatFormatting.WHITE)));
        send(player, updateLine(snapshot));
        send(player, Component.literal("Author: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(snapshot.author()).withStyle(ChatFormatting.WHITE)));
        send(player, Component.literal("-------------------------------------------").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component updateLine(Snapshot snapshot) {
        MutableComponent label = Component.literal("Is updated? ").withStyle(ChatFormatting.GRAY);
        if (snapshot.status() == Status.FAILED) {
            return label.append(Component.literal("Failed to check releases").withStyle(ChatFormatting.RED));
        }

        if (!snapshot.updateAvailable()) {
            return label.append(Component.literal("Yes").withStyle(ChatFormatting.GREEN));
        }

        String message = "Your version is " + snapshot.localVersion() + ", but there is an update number " + snapshot.releaseVersion();
        URI releaseUri = URI.create(snapshot.releaseUrl());
        MutableComponent update = Component.literal(message).withStyle(style -> style
            .withColor(ChatFormatting.RED)
            .withUnderlined(true)
            .withClickEvent(new ClickEvent.OpenUrl(releaseUri))
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to open the IssuesFix release"))));
        return label.append(update);
    }

    private static String status(Snapshot snapshot) {
        return snapshot.status() == Status.WORKING ? "Working" : "Failed";
    }

    private static void send(LocalPlayer player, Component component) {
        if (player != null) {
            player.displayClientMessage(component, false);
        }
    }
}
