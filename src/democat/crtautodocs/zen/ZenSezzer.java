package democat.crtautodocs.zen;

import java.nio.file.Path;

import democat.crtautodocs.Main;
import democat.crtautodocs.Markdown;

public class ZenSezzer implements Comparable<ZenSezzer> {
	public String type;
	public String name;
	
	public ZenSezzer(String type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return this.type + " " + this.name;
	}
	
	public String toMarkdownString(Path dir) {
		return Markdown.formatZenClazzLink(dir, Main.getFromJava(this.type).orElse(null), true).orElse(this.type) + " " + this.name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ZenSezzer)) return false;
		ZenSezzer zs = (ZenSezzer) obj;
		return this.name.equals(zs.name);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public int compareTo(ZenSezzer o) {
		return this.name.compareTo(o.name);
	}
}
