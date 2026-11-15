package com.hackpanel;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class PanelScreen extends Screen {
    private TextFieldWidget fx, fy, fz;

    public PanelScreen() {
        super(Text.literal("Hack Panel"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 85;

        toggle("Fly", y, () -> HackPanelMod.fly, v -> HackPanelMod.fly = v);
        y += 24;

        addDrawableChild(ButtonWidget.builder(flyLabel(), b -> {
            HackPanelMod.flyIdx = (HackPanelMod.flyIdx + 1) % HackPanelMod.FLY_SPEEDS.length;
            b.setMessage(flyLabel());
        }).dimensions(cx - 100, y, 200, 20).build());
        y += 24;

        toggle("Speed", y, () -> HackPanelMod.speed, v -> HackPanelMod.speed = v);
        y += 24;

        toggle("Click TP (tecla R)", y, () -> HackPanelMod.clickTp, v -> HackPanelMod.clickTp = v);
        y += 34;

        fx = field(cx - 100, y); fy = field(cx - 31, y); fz = field(cx + 38, y);
        if (client != null && client.player != null) {
            fx.setText(String.format(Locale.ROOT, "%.0f", client.player.getX()));
            fy.setText(String.format(Locale.ROOT, "%.0f", client.player.getY()));
            fz.setText(String.format(Locale.ROOT, "%.0f", client.player.getZ()));
        }
        y += 24;

        addDrawableChild(ButtonWidget.builder(Text.literal("Teleport a X Y Z"), b -> {
            try {
                HackPanelMod.teleport(client.player,
                        Double.parseDouble(fx.getText().trim()),
                        Double.parseDouble(fy.getText().trim()),
                        Double.parseDouble(fz.getText().trim()));
            } catch (NumberFormatException ignored) { }
        }).dimensions(cx - 100, y, 200, 20).build());
    }

    private TextFieldWidget field(int x, int y) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, 62, 20, Text.empty());
        addDrawableChild(f);
        return f;
    }

    private Text flyLabel() {
        return Text.literal("Fly speed: x" + (HackPanelMod.flyIdx + 1));
    }

    private void toggle(String name, int y, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
        }).dimensions(width / 2 - 100, y, 200, 20).build());
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 110, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
