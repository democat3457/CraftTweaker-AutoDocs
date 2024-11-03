package democat.crtautodocs.zen;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import democat.crtautodocs.Main;
import democat.crtautodocs.Markdown;

public class ZenMezzod implements Comparable<ZenMezzod> {
	public boolean isStatic;
	public String returnType;
	public String name;
	public ZenParazz[] params;
	
	public ZenMezzod(boolean isStatic, String returnType, String name, ZenParazz... params) {
		this.isStatic = isStatic;
		this.returnType = returnType;
		this.name = name;
		this.params = params;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return (this.isStatic ? "static " : "") + this.returnType + " " + this.name + "(" + String.join(", ", Arrays.stream(params).map(ZenParazz::toString).collect(Collectors.toList())) + ")";
	}
	
	public String toMarkdownString(Path dir) {
		return (this.isStatic ? "static " : "") 
				+ Markdown.formatZenClazzLink(dir, Main.getFromJava(this.returnType).orElse(null), true).orElse(this.returnType) 
				+ " "
				+ this.name
				+ "("
				+ String.join(", ", Arrays.stream(params).map(p -> p.toMarkdownString(dir)).collect(Collectors.toList()))
				+ ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ZenMezzod)) return false;
		ZenMezzod zm = (ZenMezzod) obj;
		return this.isStatic == zm.isStatic && this.name.equals(zm.name) && Arrays.equals(this.params, zm.params);
	}
	
	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + ((params != null) ? params.hashCode() : 0);
		result = 31 * result + (isStatic ? 1 : 0);
		return result;
	}

	@Override
	public int compareTo(ZenMezzod o) {
		int result = Boolean.valueOf(this.isStatic).compareTo(Boolean.valueOf(o.isStatic));
		if (result != 0) return result;
		
		result = this.name.compareTo(o.name);
		if (result != 0) return result;
		
		result = Integer.valueOf(this.params.length).compareTo(Integer.valueOf(o.params.length));
		if (result != 0) return result;
		
		for (int i = 0; i < this.params.length; i++) {
			result = this.params[i].type.compareTo(o.params[i].type);
			if (result != 0) return result;
			
			result = this.params[i].name.compareTo(o.params[i].name);
			if (result != 0) return result;
		}
		
		return 0;
	}
}
