package com.bestialMania.object.gui.text;

import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.MemoryManager;
import com.bestialMania.rendering.Texture;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Font {
    private Texture texture;
    private Map<Character,FontCharacter> characters = new HashMap<>();
    private int lineHeight;

    public Font(MemoryManager mm, String fontFile, String fontTexture) {
        //the texture atlas
        texture = Texture.loadImageTexture2D(mm,fontTexture);

        //load all characters from font file
        try {
            Scanner scan = new Scanner(new File(fontFile));
            int width = 0;
            while(scan.hasNext()) {
                String token = scan.next();
                //this line has details about the font
                if(token.equals("common")) {
                    //line height
                    String[] split1 = scan.next().split("=");
                    lineHeight = Integer.parseInt(split1[1]);
                    scan.next();
                    //width
                    String[] split2 = scan.next().split("=");
                    width = Integer.parseInt(split2[1]);
                }
                //this line is a character
                else if(token.equals("char")) {
                    char c = (char) Integer.parseInt(scan.next().split("=")[1]);
                    FontCharacter character = new FontCharacter(width,c,
                            Integer.parseInt(scan.next().split("=")[1]),
                            Integer.parseInt(scan.next().split("=")[1]),
                            Integer.parseInt(scan.next().split("=")[1]),
                            Integer.parseInt(scan.next().split("=")[1]),
                            Integer.parseInt(scan.next().split("=")[1]),
                            Integer.parseInt(scan.next().split("=")[1]),
                            Integer.parseInt(scan.next().split("=")[1])
                    );
                    characters.put(c,character);
                }
                scan.nextLine();
            }
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error loading font");
            System.exit(-1);
        }

    }

    /**
     * Get the texture of the font
     */
    public Texture getTexture() {return texture;}

    /**
     * Get the line height
     */
    public int getLineHeight() {return lineHeight;}

    /**
     * Gets the FontCharacter from the map given a character in a string
     */
    public FontCharacter getCharacter(char c) {
        if(characters.containsKey(c)) {
            return characters.get(c);
        }else{
            return characters.get(0);
        }
    }
}
