package me.xoq.cortex.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import me.xoq.cortex.command.Commands;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static me.xoq.cortex.CortexClient.mc;

@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggesterMixin {
    @Shadow private ParseResults<CommandSource> parse;
    @Shadow @Final TextFieldWidget textField;
    @Shadow boolean completingSuggestions;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private ChatInputSuggestor.SuggestionWindow window;

    @Shadow private void showCommandSuggestions() { }

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false), cancellable = true)
    public void onRefresh(CallbackInfo ci, @Local StringReader reader) {
        int prefixLen = Commands.PREFIX.length();

        // Only activate when input starts with command prefix
        if (reader.canRead(prefixLen) && reader.getString().startsWith(Commands.PREFIX, reader.getCursor())) {
            // Skip prefix
            reader.setCursor(reader.getCursor() + prefixLen);

            // Parse once if needed
            if (parse == null) {
                parse = Commands.getDispatcher().parse(reader,
                        Objects.requireNonNull(mc.getNetworkHandler()).getCommandSource());
            }

            int cursor = textField.getCursor();
            // If suggestions are not in progress already, request them
            if (cursor >= prefixLen && (this.window == null || !this.completingSuggestions)) {
                this.pendingSuggestions = Commands.getDispatcher().getCompletionSuggestions(this.parse, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.showCommandSuggestions();
                    }
                });
            }

            // Prevent vanilla suggestion logic
            ci.cancel();
        }
    }
}