/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.frederikam.gensokyobot;

import com.frederikam.gensokyobot.util.DiscordUtil;
import com.frederikam.gensokyobot.util.DistributionEnum;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    
    public static Config CONFIG = null;

    public static String DEFAULT_PREFIX = "--";
    public static String GENSOKYO_RADIO_STREAM_URL = "https://gensokyoradio.net/GensokyoRadio.m3u";

    private final DistributionEnum distribution;
    private String prefix;
    private int numShards;
    private String token;
    private String streamUrl;


    @SuppressWarnings("unchecked")
    public Config(File credentialsFile, File configFile) {
        try {
            Yaml yaml = new Yaml();
            String credsFileStr = FileUtils.readFileToString(credentialsFile, "UTF-8");
            String configFileStr = FileUtils.readFileToString(configFile, "UTF-8");
            //remove those pesky tab characters so a potential json file is YAML conform
            credsFileStr = credsFileStr.replaceAll("\t", "");
            configFileStr = configFileStr.replaceAll("\t", "");
            Map<String, Object> creds = (Map<String, Object>) yaml.load(credsFileStr);
            Map<String, Object> config = (Map<String, Object>) yaml.load(configFileStr);
            //avoid null values, rather change them to empty strings
            creds.keySet().forEach((String key) -> creds.putIfAbsent(key, ""));
            config.keySet().forEach((String key) -> config.putIfAbsent(key, ""));


            // Determine distribution
            if ((boolean) config.getOrDefault("development", false)) {//Determine distribution
                distribution = DistributionEnum.DEVELOPMENT;
            } else {
                distribution = DistributionEnum.MUSIC;
            }

            log.info("Determined distribution: " + distribution);

            token = (String) creds.get("token");
            numShards = DiscordUtil.getRecommendedShardCount(token);
            prefix = (String) config.getOrDefault("prefix", DEFAULT_PREFIX);
            streamUrl = (String) config.getOrDefault("streamUrl", GENSOKYO_RADIO_STREAM_URL);

        } catch (IOException | UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    static void loadDefaultConfig(int scope) throws IOException {
        Config.CONFIG = new Config(
                loadConfigFile("credentials"),
                loadConfigFile("config")
        );
    }

    /**
     * Makes sure the requested config file exists in the current format. Will attempt to migrate old formats to new ones
     * old files will be renamed to filename.ext.old to preserve any data
     *
     * @param name relative name of a config file, without the file extension
     * @return a handle on the requested file
     */
    private static File loadConfigFile(String name) throws IOException {
        String yamlPath = "./" + name + ".yaml";
        String jsonPath = "./" + name + ".json";
        File yamlFile = new File(yamlPath);
        if (!yamlFile.exists() || yamlFile.isDirectory()) {
            log.warn("Could not find file '" + yamlPath + "', looking for legacy '" + jsonPath + "' to rewrite");
            File json = new File(jsonPath);
            if (!json.exists() || json.isDirectory()) {
                //file is missing
                log.error("No " + name + " file is present. Bot cannot run without it. Check the documentation.");
                throw new FileNotFoundException("Neither '" + yamlPath + "' nor '" + jsonPath + "' present");
            } else {
                //rewrite the json to yaml
                Yaml yaml = new Yaml();
                String fileStr = FileUtils.readFileToString(json, "UTF-8");
                //remove tab character from json file to make it a valid YAML file
                fileStr = fileStr.replaceAll("\t", "");
                @SuppressWarnings("unchecked")
                Map<String, Object> configFile = (Map) yaml.load(fileStr);
                yaml.dump(configFile, new FileWriter(yamlFile));
                Files.move(Paths.get(jsonPath), Paths.get(jsonPath + ".old"), REPLACE_EXISTING);
                log.info("Migrated file '" + jsonPath + "' to '" + yamlPath + "'");
            }
        }

        return yamlFile;
    }

    public DistributionEnum getDistribution() {
        return distribution;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getNumShards() {
        return numShards;
    }

    public String getToken() {
        return token;
    }

    public String getStreamUrl() {
        return streamUrl;
    }
}
