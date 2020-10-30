/*
Original code by Juuxel, part of https://github.com/CottonMC/LibGui

MIT License

Copyright (c) 2018 The Cotton Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package me.steven.indrev;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LibGuiClient;
import io.github.cottonmc.cotton.gui.client.NinePatch;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class WCustomTabPanel extends WPanel {
    private static final int TAB_PADDING = 4;
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 25;
    private static final int PANEL_PADDING = 8; // The padding of BackgroundPainter.VANILLA
    private static final int ICON_SIZE = 16;
    private final WBox tabRibbon = new WBox(Axis.HORIZONTAL).setSpacing(1);
    private final List<WTab> tabWidgets = new ArrayList<>();
    private final WCardPanel mainPanel = new WCardPanel();

    /**
     * Constructs a new tab panel.
     */
    public WCustomTabPanel() {
        add(tabRibbon, 0, 0);
        add(mainPanel, PANEL_PADDING, TAB_HEIGHT + PANEL_PADDING);
    }

    public void add(WWidget widget, int x, int y) {
        children.add(widget);
        widget.setParent(this);
        widget.setLocation(x, y);
        expandToFit(widget);
    }

    @Override
    public WPanel setBackgroundPainter(BackgroundPainter painter) {
        return super.setBackgroundPainter(
                BackgroundPainter.createLightDarkVariants(
                        new NinePatch(new Identifier("libgui", "textures/widget/panel_light.png")).setPadding(0).setTopPadding(-25),
                        new NinePatch(new Identifier("libgui", "textures/widget/panel_dark.png")).setPadding(8).setTopPadding(-25)
                ));
    }

    // dont bother it's just bad code ik
    public WPanel setForceBackgroundPainter(BackgroundPainter painter) {
        return super.setBackgroundPainter(painter);
    }

    /**
     * Adds a tab to this panel.
     *
     * @param tab the added tab
     */
    public void add(WCustomTabPanel.Tab tab) {
        WTab tabWidget = new WTab(tab);

        if (tabWidgets.isEmpty()) {
            tabWidget.selected = true;
        }

        tabWidgets.add(tabWidget);
        tabRibbon.add(tabWidget, TAB_WIDTH, TAB_HEIGHT + TAB_PADDING);
        mainPanel.add(tab.getWidget());
    }

    /**
     * Configures and adds a tab to this panel.
     *
     * @param widget       the contained widget
     * @param configurator the tab configurator
     */
    public void add(WWidget widget, Consumer<WCustomTabPanel.Tab.Builder> configurator) {
        WCustomTabPanel.Tab.Builder builder = new WCustomTabPanel.Tab.Builder(widget);
        configurator.accept(builder);
        add(builder.build());
    }

    @Override
    public void setSize(int x, int y) {
        super.setSize(x, y);
        tabRibbon.setSize(x, TAB_HEIGHT);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void addPainters() {
        super.addPainters();
    }

    /**
     * The data of a tab.
     */
    public static class Tab {
        private final Text title;
        private final Icon icon;
        private final WWidget widget;
        private final Consumer<TooltipBuilder> tooltip;

        /**
         * Constructs a tab.
         *
         * @param title   the tab title
         * @param icon    the tab icon
         * @param widget  the widget contained in the tab
         * @param tooltip the tab tooltip
         * @throws IllegalArgumentException if both the title and the icon are null
         * @throws NullPointerException     if either the widget or the tooltip is null
         */
        public Tab(Text title, Icon icon, WWidget widget, Consumer<TooltipBuilder> tooltip) {
            if (title == null && icon == null) {
                throw new IllegalArgumentException("A tab must have a title or an icon");
            }

            this.title = title;
            this.icon = icon;
            this.widget = Objects.requireNonNull(widget, "widget");
            this.tooltip = Objects.requireNonNull(tooltip, "tooltip");
        }

        /**
         * Gets the title of this tab.
         *
         * @return the title, or null if there's no title
         */
        public Text getTitle() {
            return title;
        }

        /**
         * Gets the icon of this tab.
         *
         * @return the icon, or null if there's no title
         */
        public Icon getIcon() {
            return icon;
        }

        /**
         * Gets the contained widget of this tab.
         *
         * @return the contained widget
         */
        public WWidget getWidget() {
            return widget;
        }

        /**
         * Adds this widget's tooltip to the {@code tooltip} builder.
         *
         * @param tooltip the tooltip builder
         */
        public void addTooltip(TooltipBuilder tooltip) {
            this.tooltip.accept(tooltip);
        }

        /**
         * A builder for tab data.
         */
        public static final class Builder {
            private Text title;
            private Icon icon;
            private final WWidget widget;
            private final List<Consumer<TooltipBuilder>> tooltip = new ArrayList<>();
            private static final Consumer<TooltipBuilder> DEFAULT_TOOLTIP = builder -> {
            };

            /**
             * Constructs a new tab data builder.
             *
             * @param widget the contained widget
             * @throws NullPointerException if the widget is null
             */
            public Builder(WWidget widget) {
                this.widget = Objects.requireNonNull(widget, "widget");
            }

            /**
             * Sets the tab title.
             *
             * @param title the new title
             * @return this builder
             * @throws NullPointerException if the title is null
             */
            public WCustomTabPanel.Tab.Builder title(Text title) {
                this.title = Objects.requireNonNull(title, "title");
                return this;
            }

            /**
             * Sets the tab icon.
             *
             * @param icon the new icon
             * @return this builder
             * @throws NullPointerException if the icon is null
             */
            public WCustomTabPanel.Tab.Builder icon(Icon icon) {
                this.icon = Objects.requireNonNull(icon, "icon");
                return this;
            }

            /**
             * Adds lines to the tab's tooltip.
             *
             * @param lines the added lines
             * @return this builder
             * @throws NullPointerException if the line array is null
             */
            public WCustomTabPanel.Tab.Builder tooltip(Text... lines) {
                Objects.requireNonNull(lines, "lines");
                tooltip.add(builder -> builder.add(lines));

                return this;
            }

            /**
             * Adds lines to the tab's tooltip.
             *
             * @param lines the added lines
             * @return this builder
             * @throws NullPointerException if the line collection is null
             */
            public WCustomTabPanel.Tab.Builder tooltip(Collection<? extends Text> lines) {
                Objects.requireNonNull(lines, "lines");
                tooltip.add(builder -> builder.add(lines.toArray(new Text[0])));
                return this;
            }

            /**
             * Builds a tab from this builder.
             *
             * @return the built tab
             * @see WCustomTabPanel.Tab#Tab(Text, Icon, WWidget, Consumer)
             */
            public WCustomTabPanel.Tab build() {
                Consumer<TooltipBuilder> tooltip = DEFAULT_TOOLTIP;

                if (!this.tooltip.isEmpty()) {
                    tooltip = builder -> {
                        for (Consumer<TooltipBuilder> entry : this.tooltip) {
                            entry.accept(builder);
                        }
                    };
                }

                return new WCustomTabPanel.Tab(title, icon, widget, tooltip);
            }
        }
    }

    private final class WTab extends WWidget {
        private final WCustomTabPanel.Tab data;
        boolean selected = false;

        WTab(WCustomTabPanel.Tab data) {
            this.data = data;
        }

        @Override
        public boolean canFocus() {
            return true;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public void onClick(int x, int y, int button) {
            super.onClick(x, y, button);

            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            for (WTab tab : tabWidgets) {
                tab.selected = (tab == this);
            }

            mainPanel.setSelectedCard(data.getWidget());
            WCustomTabPanel.this.layout();
        }

        @Environment(EnvType.CLIENT)
        @Override
        public void onKeyPressed(int ch, int key, int modifiers) {
            if (isActivationKey(ch)) {
                onClick(0, 0, 0);
            }
        }

        @Environment(EnvType.CLIENT)
        @Override
        public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
            Text title = data.getTitle();
            Icon icon = data.getIcon();

            if (title != null) {
                int width = TAB_WIDTH + renderer.getWidth(title);
                if (icon == null) width = Math.max(TAB_WIDTH, width - ICON_SIZE);

                if (this.width != width) {
                    setSize(width, this.height);
                    getParent().layout();
                }
            }

            (selected ? WCustomTabPanel.Painters.SELECTED_TAB : WCustomTabPanel.Painters.UNSELECTED_TAB).paintBackground(x, y, this);
            if (isFocused()) {
                (selected ? WCustomTabPanel.Painters.SELECTED_TAB_FOCUS_BORDER : Painters.UNSELECTED_TAB_FOCUS_BORDER).paintBackground(x, y, this);
            }

            int iconX = 6;

            if (title != null) {
                int titleX = (icon != null) ? iconX + ICON_SIZE + 1 : 0;
                int titleY = (height - TAB_PADDING - renderer.fontHeight) / 2 + 1;
                int width = (icon != null) ? this.width - iconX - ICON_SIZE : this.width;
                HorizontalAlignment align = (icon != null) ? HorizontalAlignment.LEFT : HorizontalAlignment.CENTER;

                int color;
                if (LibGuiClient.config.darkMode) {
                    color = WLabel.DEFAULT_DARKMODE_TEXT_COLOR;
                } else {
                    color = selected ? WLabel.DEFAULT_TEXT_COLOR : 0xEEEEEE;
                }

                ScreenDrawing.drawString(matrices, title.asOrderedText(), align, x + titleX, y + titleY, width, color);
            }

            if (icon != null) {
                icon.paint(matrices, x + iconX, (y + (height - TAB_PADDING - ICON_SIZE) / 2) + 1, ICON_SIZE);
            }
        }

        @Override
        public void addTooltip(TooltipBuilder tooltip) {
            data.addTooltip(tooltip);
        }
    }

    /**
     * Internal background painter instances for tabs.
     */
    @Environment(EnvType.CLIENT)
    final static class Painters {
        static final BackgroundPainter SELECTED_TAB = BackgroundPainter.createLightDarkVariants(
                BackgroundPainter.createNinePatch(new Identifier("indrev", "textures/gui/selected_light.png")).setTopPadding(2),
                BackgroundPainter.createNinePatch(new Identifier("libgui", "textures/widget/tab/selected_dark.png")).setTopPadding(2)
        );

        static final BackgroundPainter UNSELECTED_TAB = BackgroundPainter.createLightDarkVariants(
                BackgroundPainter.createNinePatch(new Identifier("indrev", "textures/gui/unselected_light.png")),
                BackgroundPainter.createNinePatch(new Identifier("libgui", "textures/widget/tab/unselected_dark.png"))
        );

        static final BackgroundPainter SELECTED_TAB_FOCUS_BORDER = BackgroundPainter.createNinePatch(new Identifier("libgui", "textures/widget/tab/focus.png")).setTopPadding(2);
        static final BackgroundPainter UNSELECTED_TAB_FOCUS_BORDER = BackgroundPainter.createNinePatch(new Identifier("libgui", "textures/widget/tab/focus.png"));
    }
}

