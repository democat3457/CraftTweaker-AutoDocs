package democat.crtautodocs.zen;

import java.nio.file.Path;
import java.util.Optional;

import democat.crtautodocs.Main;
import democat.crtautodocs.Markdown;

public class ZenParazz {
	public String type;
	public String name;
	public Optional<String> optDefault;
	
	public ZenParazz(String type, String name) {
		this(type, name, (String) null);
	}
	
	public ZenParazz(String type, String name, String optDefault) {
		this.type = type;
		this.name = name;
		this.optDefault = Optional.ofNullable(optDefault);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return (optDefault.isPresent() ? "@Optional(" + optDefault.get() + ") " : "") + this.type + " " + this.name;
	}
	
	public String toMarkdownString(Path dir) {
		return (optDefault.isPresent() ? "@Optional(" + optDefault.get() + ") " : "") + Markdown.formatZenClazzLink(dir, Main.getFromJava(this.type).orElse(null), true).orElse(this.type) + " " + this.name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ZenParazz)) return false;
		ZenParazz zg = (ZenParazz) obj;
		return this.name.equals(zg.name);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	public static String getDefaultOptionalForType(String type) {
		switch (type.toLowerCase()) {
			case "byte":
			case "short":
			case "int":
			case "long":
			case "float":
			case "double":
			case "char":
				return "0";
			case "boolean":
				return "false";
			case "string":
				return "\"\"";
			default:
				return "null";
		}
	}
}
