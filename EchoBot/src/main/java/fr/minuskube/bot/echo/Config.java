package fr.minuskube.bot.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private String token;
    private String prefix;
    private String registerRole;

    public void load(File file) {
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while((line = reader.readLine()) != null) {
                String key = line.split(":")[0].trim();
                String value = line.split(":")[1].trim();

                switch(key.toLowerCase()) {
                    case "token": this.token = value;
                        break;
                    case "prefix": this.prefix = value;
                        break;
                    case "registerrole": this.registerRole = value;
                        break;
                }
            }
        } catch(IOException e) {
            LOGGER.error("Can't load config: ", e);
        }
    }

    public void saveDefault(File file) throws IOException {
        if(!file.exists())
            file.createNewFile();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("token: PUT_YOUR_TOKEN_HERE");
            writer.newLine();
            writer.write("prefix: !");
            writer.newLine();
            writer.write("registerRole: XXX");
        } catch(IOException e) {
            LOGGER.error("Can't save default config: ", e);
        }
    }

    public String getToken() { return token; }
    public String getPrefix() { return prefix; }
    public String getRegisterRole() { return registerRole; }

}
