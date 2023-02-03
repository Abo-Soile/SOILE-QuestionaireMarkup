package fi.abo.kogni.soile2.qmarkup;

import java.util.HashMap;

public class TextStyle {
    
	private String type;
	private String value;
	
	public TextStyle(String type, String value)
	{
		this.type = type;
		this.value = value;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public boolean isValid()
	{
		return !this.type.equals("");
	}
	
    public static boolean defined(String name) {
        return styles.containsKey(name);
    }

    public static TextStyle get(String name) {
        String key = name.toLowerCase().trim();
        if (styles.containsKey(key)) {
            return styles.get(key);
        }
        return new TextStyle("", "");
    }
    
    public static TextStyle indent() {
        return get("indent");
    }

    public static TextStyle aqua() {
        return get("aqua");
    }

    public static TextStyle black() {
        return get("black");
    }

    public static TextStyle blue() {
        return get("blue");
    }

    public static TextStyle fuchsia() {
        return get("fuchsia");
    }

    public static TextStyle gray() {
        return get("gray");
    }

    public static TextStyle green() {
        return get("green");
    }

    public static TextStyle lime() {
        return get("lime");
    }

    public static TextStyle maroon() {
        return get("maroon");
    }

    public static TextStyle navy() {
        return get("navy");
    }

    public static TextStyle olive() {
        return get("olive");
    }

    public static TextStyle orange() {
        return get("orange");
    }

    public static TextStyle purple() {
        return get("purple");
    }

    public static TextStyle red() {
        return get("red");
    }

    public static TextStyle silver() {
        return get("silver");
    }

    public static TextStyle teal() {
        return get("teal");
    }

    public static TextStyle monospace() {
        return get("monospace");
    }

    public static TextStyle sansserif() {
        return get("sansserif");
    }

    public static TextStyle serif() {
        return get("serif");
    }

    public static TextStyle small() {
        return get("small");
    }

    public static TextStyle large() {
        return get("large");
    }

    public static TextStyle italic() {
        return get("italic");
    }

    public static TextStyle bold() {
        return get("bold");
    }

    public static TextStyle linethrough() {
        return get("linethrough");
    }

    public static TextStyle overline() {
        return get("overline");
    }

    public static TextStyle underline() {
        return get("underline");
    }

    public static TextStyle capitalize() {
        return get("capitalize");
    }

    public static TextStyle lowercase() {
        return get("lowercase");
    }

    public static TextStyle uppercase() {
        return get("uppercase");
    }

    private static HashMap<String, TextStyle> styles;

    static {
        styles = new HashMap<>();
        styles.put("aqua", new TextStyle("color","aqua"));
        styles.put("black", new TextStyle("color","black"));
        styles.put("blue", new TextStyle("color","blue"));
        styles.put("bold", new TextStyle("font-weight","bold"));
        styles.put("capitalize", new TextStyle("text-transform","capitalize"));
        styles.put("fuchsia", new TextStyle("color","fuchsia"));
        styles.put("gray", new TextStyle("color","gray"));
        styles.put("green", new TextStyle("color","green"));
        styles.put("indent", new TextStyle("margin-left","1em"));
        styles.put("italic", new TextStyle("font-style","italic"));
        styles.put("large", new TextStyle("font-size","large"));
        styles.put("lime", new TextStyle("color","lime"));
        styles.put("linethrough", new TextStyle("text-decoration","line-through"));
        styles.put("lowercase", new TextStyle("text-transform","lowercase"));
        styles.put("maroon", new TextStyle("color","maroon"));
        styles.put("monospace", new TextStyle("font-family","monospace"));
        styles.put("navy", new TextStyle("color","navy"));
        styles.put("olive", new TextStyle("color","olive"));
        styles.put("orange", new TextStyle("color","orange"));
        styles.put("overline", new TextStyle("text-decoration","overline"));
        styles.put("purple", new TextStyle("color","purple"));
        styles.put("red", new TextStyle("color","red"));
        styles.put("sansserif", new TextStyle("font-family","sans-serif"));
        styles.put("serif", new TextStyle("font-family","serif"));
        styles.put("silver", new TextStyle("color","silver"));
        styles.put("small", new TextStyle("font-size","smaller"));
        styles.put("teal", new TextStyle("color","teal"));
        styles.put("underline", new TextStyle("text-decoration","underline"));
        styles.put("uppercase", new TextStyle("text-transform","uppercase"));
    }

}
