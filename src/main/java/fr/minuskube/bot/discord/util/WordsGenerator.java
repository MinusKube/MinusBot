package fr.minuskube.bot.discord.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordsGenerator.class);

    private Random random = new Random();

    private List<String> verbs;
    private List<String> adjectives;
    private List<String> nouns;

    public WordsGenerator() {
        verbs = readFile(getClass().getResourceAsStream("/words/verbs.txt"));
        adjectives = readFile(getClass().getResourceAsStream("/words/adjectives.txt"));
        nouns = readFile(getClass().getResourceAsStream("/words/nouns.txt"));
    }

    private List<String> readFile(InputStream in) {
        List<String> list = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while((line = reader.readLine()) != null)
                list.add(line.trim());
        } catch(IOException e) {
            LOGGER.error("Cannot read file: ", e);
        }

        return list;
    }

    public String randomVerb() { return verbs.get(random.nextInt(verbs.size())); }
    public String randomAdjective() { return adjectives.get(random.nextInt(adjectives.size())); }
    public String randomNoun() { return nouns.get(random.nextInt(nouns.size())); }


    private static WordsGenerator instance;
    public static WordsGenerator instance() {
        if(instance == null)
            instance = new WordsGenerator();

        return instance;
    }

}
