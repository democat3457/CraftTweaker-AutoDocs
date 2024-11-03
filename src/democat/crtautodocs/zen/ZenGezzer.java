package democat.crtautodocs.zen;

import java.nio.file.Path;

import democat.crtautodocs.*;

public class ZenGezzer implements Comparable<ZenGezzer> {
	public String type;
	public String name;
	
	public ZenGezzer(String type, String name) {
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
		if (!(obj instanceof ZenGezzer)) return false;
		ZenGezzer zg = (ZenGezzer) obj;
		return this.name.equals(zg.name);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public int compareTo(ZenGezzer o) {
		return this.name.compareTo(o.name);
	}
}
