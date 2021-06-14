package com.convallyria.floatybarrels.translation;

import com.convallyria.floatybarrels.FloatyBarrels;
import me.clip.placeholderapi.PlaceholderAPI;
import net.islandearth.languagy.api.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum Translations {
    BARREL_SUNK("&cYer barrel be sunk!"),
    BARREL_FLOATING("&aYer now be floating in a barrel!"),
    BARREL_PATH_BLOCKED("&cThe path be blocked!");

    private final String defaultValue;
    private final boolean isList;

    Translations(String defaultValue) {
        this.defaultValue = defaultValue;
        this.isList = false;
    }

    Translations(String defaultValue, boolean isList) {
        this.defaultValue = defaultValue;
        this.isList = isList;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isList() {
        return isList;
    }

    private String getPath() {
        return this.toString().toLowerCase();
    }

    public void send(Player player) {
        String message = JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator().getTranslationFor(player, this.getPath());
        player.sendMessage(message);
    }

    public void send(Player player, String... values) {
        String message = JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator().getTranslationFor(player, this.getPath());
        message = this.setPapi(player, replaceVariables(message, values));
        player.sendMessage(message);
    }

    public void sendList(Player player) {
        List<String> messages = JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator().getTranslationListFor(player, this.getPath());
        messages.forEach(player::sendMessage);
    }

    public void sendList(Player player, String... values) {
        List<String> messages = JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator().getTranslationListFor(player, this.getPath());
        messages.forEach(message -> {
            message = this.setPapi(player, replaceVariables(message, values));
            player.sendMessage(message);
        });
    }

    public String get(Player player) {
        return this.setPapi(player, JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator().getTranslationFor(player, this.getPath()));
    }

    public String get(Player player, String... values) {
        String message = JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator().getTranslationFor(player, this.getPath());
        message = replaceVariables(message, values);
        return this.setPapi(player, message);
    }

    public List<String> getList(Player player) {
        List<String> list = new ArrayList<>();
        JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator().getTranslationListFor(player, this.getPath()).forEach(text -> list.add(this.setPapi(player, text)));
        return list;
    }

    public List<String> getList(Player player, String... values) {
        List<String> messages = new ArrayList<>();
        JavaPlugin.getPlugin(FloatyBarrels.class).getTranslator()
                .getTranslationListFor(player, this.getPath())
                .forEach(message -> messages.add(this.setPapi(player, replaceVariables(message, values))));
        return messages;
    }

    public static void generateLang(FloatyBarrels plugin) {
        File lang = new File(plugin.getDataFolder() + "/lang/");
        lang.mkdirs();

        for (Language language : Language.values()) {
            try {
                plugin.saveResource("lang/" + language.getCode() + ".yml", false);
                plugin.getLogger().info("Generated " + language.getCode() + ".yml");
            } catch (IllegalArgumentException ignored) { }

            File file = new File(plugin.getDataFolder() + "/lang/" + language.getCode() + ".yml");
            if (file.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                for (Translations key : values()) {
                    if (config.get(key.toString().toLowerCase()) == null) {
                        plugin.getLogger().warning("No value in translation file for key "
                                + key.toString() + " was found. Regenerate language files?");
                    }
                }
            }
        }
    }

    @NotNull
    private String replaceVariables(String message, String... values) {
        String modifiedMessage = message;
        for (int i = 0; i < 10; i++) {
            if (values.length > i) modifiedMessage = modifiedMessage.replaceAll("%" + i, values[i]);
            else break;
        }

        return modifiedMessage;
    }

    @NotNull
    private String setPapi(Player player, String message) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }
}
