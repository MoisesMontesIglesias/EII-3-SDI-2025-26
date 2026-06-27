package com.uniovi.sdi.gradeManager.pageobjects;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

public class PO_Properties {
    public static int getSPANISH() {
        return SPANISH;
    }

    public static int getENGLISH() {
        return ENGLISH;
    }

    static private String Path;
    static final int SPANISH = 0;
    static final int ENGLISH = 1;
    static final Locale[] idioms =  {Locale.of("es", "ES"),Locale.of("en", "US")};

        //static Properties p = new Properties();
	public PO_Properties(String Path) //throws FileNotFoundException, IOException 
        {
            PO_Properties.Path = Path;
        }
        //
        // locale is de index in idioms array.
        //
        public String getString (String prop,int locale){
        ResourceBundle bundle = ResourceBundle.getBundle(Path, idioms[locale]);
        String value = bundle.getString(prop);
        String result;
        result = new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        return result;
    }


    }
