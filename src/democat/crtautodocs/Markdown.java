package democat.crtautodocs;

import java.nio.file.Path;
import java.util.Optional;

import democat.crtautodocs.zen.*;

public class Markdown {
	public static Path formatZenClazzPath(Path baseFolder, ZenClazz zc) {
		String path = zc.canonicalName.replace(".", "/");
		path += ".md";
		return baseFolder.resolve(path);
	}
	
	public static Optional<String> formatZenClazzLink(Path baseFolder, ZenClazz zc) {
		return formatZenClazzLink(baseFolder, zc, false);
	}
	
	public static Optional<String> formatZenClazzLink(Path baseFolder, ZenClazz zc, boolean shortName) {
		if (zc == null) return Optional.empty();
		Path p = formatZenClazzPath(baseFolder, zc);
		return Optional.of("[" + (shortName ? zc.name : zc.canonicalName) + "](" + p.toAbsolutePath().toString() + ")");
	}
}
