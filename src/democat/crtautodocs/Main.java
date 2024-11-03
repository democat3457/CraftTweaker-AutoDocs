package democat.crtautodocs;

import democat.crtautodocs.zen.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

	// TODO: Separate methods into Util class
	
	/** Map of canonical (import) name to zenclass 			*/
	public static final Map<String, ZenClazz> zenClasses = new HashMap<>();
	/** Map of Java class name to zenclass 					*/
	public static final Map<String, ZenClazz> internalClasses = new HashMap<>();
	/** Map of Java classes that a zenclass extends 		*/
	public static final Map<ZenClazz, Set<String>> extendsClasses = new HashMap<>();
	/** Map of zenclasses that extend the given zenclass 	*/
	public static final Map<ZenClazz, Set<ZenClazz>> extendedClasses = new HashMap<>();
	
	/** Expansion classes */
	public static final List<ZenClazz> expansionClasses = new ArrayList<>();
	
	public static void main(String[] args) {
		PrintStream out = System.out;
		long startTime = System.currentTimeMillis();
		
		if (args.length < 1) {
			System.err.println("Need a file argument!");
			return;
		} else if (args.length < 2) {
			System.err.println("Need a destination folder!");
			return;
		}
		
		try (Stream<Path> paths = Files.walk(Paths.get(args[0]))) {
			paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java")).map(Path::toFile).forEach(Main::parse);
		} catch (IOException e) {
			System.err.println("IO Exception: " + e.getLocalizedMessage());
			return;
		}
		
		registerExpansions();
		postParse();
		Path p = Paths.get(args[1]);
		System.out.println("Parent dir: " + p.toAbsolutePath());
		String d = new SimpleDateFormat("yyyyMMdd_HHmmssSS").format(new Date());
		System.out.println("Date: " + d);
		p = p.resolve(d);
		System.out.println("Formatted dir: " + p.toAbsolutePath());
		write(p);
		
		System.setOut(out);
		System.out.println();
		System.out.println("Generated " + zenClasses.size() + " ZenClasses and registered " + expansionClasses.size() + " ZenExpansions in " + (System.currentTimeMillis() - startTime) + " ms.");
	}
	
	public static void write(Path dir) {
		if (!dir.toFile().exists() || !dir.toFile().isDirectory())
			dir.toFile().mkdirs();
		
		for (Map.Entry<String, ZenClazz> entry : zenClasses.entrySet()) {
			Path newPath = Markdown.formatZenClazzPath(dir, entry.getValue());
			if (!newPath.getParent().toFile().exists())
				newPath.getParent().toFile().mkdirs();
			
			File f = newPath.toFile();
			try {
				if (!f.exists())
					f.createNewFile();
				System.setOut(new PrintStream(f));
			} catch (FileNotFoundException e) {
				System.err.println("File not found! " + e.getLocalizedMessage());
			} catch (IOException e) {
				System.err.println("IO Exception for path " + f.getAbsolutePath() + ": " + e.getLocalizedMessage());
			}
			
			ZenClazz zc = entry.getValue();
			
			System.out.println("# ZenClass: " + zc.name);
			System.out.println("Java class: " + zc.internalName + "  ");
			System.out.println("**Import:** " + zc.canonicalName + "  ");
			StringBuilder sb;
			
			if (zc.extendNames.length != 0)
				System.out.println("Extends ZenClasses: " + String.join(", ", Arrays.stream(zc.extendNames).map(c -> Markdown.formatZenClazzLink(dir, getFromJava(c).orElse(null), true).orElse(c)).sorted().collect(Collectors.toList())) + "  ");
			
			if (!extendedClasses.get(zc).isEmpty()) 
				System.out.println("Direct Known Subclasses: " + String.join(", ", extendedClasses.get(zc).stream().map(z -> Markdown.formatZenClazzLink(dir, z, true).get()).sorted().collect(Collectors.toList())) + "  ");
			
			sb = new StringBuilder("## ZenGetters: ");
			for (ZenGezzer zg : inlineSort(zc.getBaseGezzers().values())) sb.append("\n - " + zg.toMarkdownString(dir));
			for (ZenGezzer zg : inlineSort(zc.getOverridenGezzers().values())) sb.append("\n - (Overriden) " + zg.toMarkdownString(dir));
			for (ZenGezzer zg : inlineSort(inlineExclude(zc.getInheritedGezzers().values(), zc.getOverridenGezzers().values()))) sb.append("\n - (Inherited) " + zg.toMarkdownString(dir));
			if (!sb.toString().equals("## ZenGetters: ")) System.out.println(sb.toString() + "\n");
			
			sb = new StringBuilder("## ZenSetters: ");
			for (ZenSezzer zs : inlineSort(zc.getBaseSezzers().values())) sb.append("\n - " + zs.toMarkdownString(dir));
			for (ZenSezzer zs : inlineSort(zc.getOverridenSezzers().values())) sb.append("\n - (Overriden) " + zs.toMarkdownString(dir));
			for (ZenSezzer zs : inlineSort(inlineExclude(zc.getInheritedSezzers().values(), zc.getOverridenSezzers().values()))) sb.append("\n - (Inherited) " + zs.toMarkdownString(dir));
			if (!sb.toString().equals("## ZenSetters: ")) System.out.println(sb.toString() + "\n");
			
			sb = new StringBuilder("## ZenMethods: ");
			for (ZenMezzod zm : inlineSort(zc.getBaseMezzods().values())) sb.append("\n - " + zm.toMarkdownString(dir));
			for (ZenMezzod zm : inlineSort(zc.getOverridenMezzods().values())) sb.append("\n - (Overriden) " + zm.toMarkdownString(dir));
			for (ZenMezzod zm : inlineSort(inlineExclude(zc.getInheritedMezzods().values(), zc.getOverridenMezzods().values()))) sb.append("\n - (Inherited) " + zm.toMarkdownString(dir));
			if (!sb.toString().equals("## ZenMethods: ")) System.out.println(sb.toString() + "\n");
			
//			System.out.println("-------------------");
		}
	}
	
	public static String getClassFromImport(String s) {
		for (int i = 0; i < s.length() - 1; i++)
			if (s.charAt(i) == '.' && Character.isUpperCase(s.charAt(i + 1)))
				return s.substring(i+1);
		return s.contains(".") ? s.split("\\.")[s.length()-1] : s;
	}
	
	public static Optional<ZenClazz> getFromJava(String javaClass) {
		return Optional.ofNullable(internalClasses.get(javaClass));
	}
	
	public static ZenClazz addClass(String name, String internalName, String... extendNames) {
		return addClass(name, internalName, false, extendNames);
	}
	
	public static ZenClazz addClass(String name, String internalName, boolean isEx, String... extendNames) {
		return addClass(new ZenClazz(name, internalName, extendNames), isEx);
	}
	
	public static ZenClazz addClass(ZenClazz zc, boolean isEx) {
		// Add expansion class to expansionClasses
		if (isEx) {
			if (zenClasses.containsKey(zc.canonicalName))
				return zenClasses.get(zc.canonicalName);
			expansionClasses.add(zc);
			return zc;
		}
		
		zenClasses.put(zc.canonicalName, zc);
		internalClasses.put(zc.internalName, zc);
		extendedClasses.put(zc, new HashSet<>());
		
		if (!extendsClasses.containsKey(zc)) extendsClasses.put(zc, new HashSet<>());
		if (zc.hasExtend()) extendsClasses.get(zc).addAll(Arrays.asList(zc.extendNames));
		
		return zc;
	}
	
	public static void registerExpansions() {
		for (ZenClazz ze : expansionClasses) {
			if (zenClasses.containsKey(ze.canonicalName)) {
				zenClasses.get(ze.canonicalName).merge(ze);
			} else {
				addClass(ze, false);
			}
		}
	}
	
	public static void postParse() {
		for (Map.Entry<ZenClazz, Set<String>> e : extendsClasses.entrySet()) {
			for (String jName : e.getValue()) {
				if (internalClasses.containsKey(jName)) {
					extendedClasses.get(internalClasses.get(jName)).add(e.getKey());
				}
			}
		}
	}
	
	public static void parse(File file) {
		
		Scanner sc;
		
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + file.getPath());
			return;
		}
		
		// Current zenclass
		ZenClazz zenClass = null;
		// If we are in a ZenExpansion
		boolean zenEx = false;
		LinkedList<String> annotationQueue = new LinkedList<>();

		while (sc.hasNext()) {
			String line = sc.nextLine().trim();
			if (line.contains("@ZenClass") && line.matches("@ZenClass\\(\"(.+)\"\\)")) {
				String s = line;
				while (s.startsWith("@")) s = sc.nextLine().trim();
				s = stripClass(s).replaceAll(" ?\\{", "");
				
				String[] e = new String[0];
				if (s.contains(" extends ")) {
					e = s.split(" extends ")[1]
							.replaceAll(", ?", ",")
							.trim()
							.split(",");
					s = s.split(" extends ")[0];
				}
				
				zenClass = addClass(line.replaceAll("@ZenClass\\(\"(.+)\"\\)", "$1"), s, e);
				continue;
			} else if (line.contains("@ZenExpansion") && line.matches("@ZenExpansion\\(\"(.+)\"\\)")) {
				String s = line;
				while (s.startsWith("@")) s = sc.nextLine().trim();
				s = stripClass(s).replaceAll(" ?\\{", "");
				
				String[] e = new String[0];
				if (s.contains(" extends ")) {
					e = s.split(" extends ")[1]
							.replaceAll(", ?", ",")
							.trim()
							.split(",");
					s = s.split(" extends ")[0];
				}
				
				zenEx = true;
				zenClass = addClass(line.replaceAll("@ZenExpansion\\(\"(.+)\"\\)", "$1"), s, true, e);
				continue;
			}
			// If we are not in a ZenClass, continue
			if (zenClass == null) continue;
			if (line.startsWith("@")) {
				// Multiple annotations on one symbol
				annotationQueue.add(line);
			} else if (!line.isEmpty()) {
				// Empty annotations on the annotated line
				while (!annotationQueue.isEmpty()) {
					String a = annotationQueue.pop();
					
					if (a.contains("@ZenGetter")) {
						if (a.matches("@ZenGetter\\(\"(.+)\"\\)")) {
							String type = stripMethod(line)
									.replace("String", "string")
									.trim()
									.split(" ")[0];
							zenClass.zenGetters.add(new ZenGezzer(type, a.replaceAll("@ZenGetter\\(\"(.+)\"\\)", "$1")));
						} else {
							String getterDec = stripMethod(line)
									.replace("String", "string")
									.replaceAll("\\(.*\\)", "")
									.trim();
							String[] temp = getterDec.split(" ");
							zenClass.zenGetters.add(new ZenGezzer(temp[0], temp[1]));
						}
						continue;
					}
					if (a.contains("@ZenSetter")) {
						if (a.matches("@ZenSetter\\(\"(.+)\"\\)")) {
							String type = stripMethod(line)
									.replace("String", "string")
									.replace("(", " ")
									.replace(")", "")
									.trim()
									.split(" ")[zenEx ? 4 : 2];
							zenClass.zenSetters.add(new ZenSezzer(type, a.replaceAll("@ZenSetter\\(\"(.+)\"\\)", "$1")));
						} else {
							String setterDec = stripMethod(line)
									.replace("String", "string")
									.replace("(", " ")
									.replace(")", "")
									.trim();
							String[] temp = setterDec.split(" ");
							zenClass.zenSetters.add(new ZenSezzer(temp[zenEx ? 4 : 2], temp[1]));
						}
						continue;
					}
					if (a.contains("@ZenMethod")) {
						boolean isStatic = a.contains("@ZenMethodStatic");
						
						String methodDec = stripMethod(line)
								.replace("String", "string")
								.replaceAll(" ?\\{", "")
								.replaceAll(" ?;", "")
								.replaceAll(", ", ",");
						ZenParazz[] params = new ZenParazz[0];
						String temp1 = methodDec.replaceFirst("\\(", " ").trim();
						if (!temp1.split(" ")[2].equals(")")) {
							String[] pTemp = methodDec.replaceFirst("\\(", ":;")
									.trim()
									.split(":;")[1]
									.replaceFirst("\\)$", "")
									.trim()
									.split(",");
							
							// Remove the first param from methods in ZenExpansions
							if (zenEx && !isStatic) pTemp = Arrays.copyOfRange(pTemp, 1, pTemp.length);
							
							List<ZenParazz> pList = new ArrayList<>();
//							System.err.println("MethodDec of \"" + methodDec + "\": \nLine: " + line + "\nParams: " + String.join(", ", pTemp));
							for (String p : pTemp) {
								if (p.contains("@Optional")) {
									if (p.contains(" = ")) {
										p = p.replaceAll("@Optional\\(.+ = (.+)\\)", "$1")
												.trim();
										String[] temp2 = p.split(" ");
										pList.add(new ZenParazz(temp2[1], temp2[2], temp2[0]));
									} else if (p.matches("\\(.+\\)")) {
										p = p.replaceAll("@Optional\\((.+)\\)", "$1")
												.trim();
										String[] temp2 = p.split(" ");
										pList.add(new ZenParazz(temp2[1], temp2[2], temp2[0]));
									} else {
										p = p.replaceAll("@Optional", "")
												.trim();
										String[] temp2 = p.split(" ");
										pList.add(new ZenParazz(temp2[0], temp2[1], ZenParazz.getDefaultOptionalForType(temp2[0])));
									}
								} else {
									String[] temp2 = p.split(" ");
									pList.add(new ZenParazz(temp2[0], temp2[1]));
								}
							}
							params = pList.toArray(new ZenParazz[0]);
						}
						String[] temp = methodDec.replaceAll("\\(", " ")
												.split(" ");
						try {
							zenClass.zenMethods.add(new ZenMezzod(isStatic, temp[0], temp[1], params));
						} catch (Exception e) {
							System.err.println("Error on " + methodDec);
						}
						continue;
					}
				}
			}
		}
		
		sc.close();
	}
	
	public static String stripMethod(String m) {
		return m.replaceAll("(public|protected|private|final|transient|abstract|default|native|static)", "")
				.replaceAll("throws .+ ?\\{", "{")
				.replace(";", "")
				.trim();
	}
	
	public static String stripClass(String c) {
		return c.replaceAll("(public|protected|private|final|abstract|interface|class)", "")
				.trim();
	}
	
	public static <T> T[] inlineSort(T[] a) {
		T[] b = Arrays.copyOf(a, a.length);
		Arrays.sort(b);
		return b;
	}
	
	public static <T extends Comparable<? super T>> Collection<T> inlineSort(Collection<T> c) {
		List<T> result = new ArrayList<T>(c);
		Collections.sort(result);
		return result;
	}
	
	public static <T extends Comparable<? super T>> Collection<T> inlineSort(Collection<T> c, Comparator<? super T> co) {
		List<T> result = new ArrayList<T>(c);
		Collections.sort(result, co);
		return result;
	}
	
	public static <T> Collection<T> inlineExclude(Collection<T> a, Collection<T> b) {
		List<T> c = new ArrayList<>(a);
		c.removeAll(b);
		return c;
	}
	
	public static <T> Collection<T> inlineInclude(Collection<T> a, Collection<T> b) {
		List<T> c = new ArrayList<>(a);
		c.retainAll(b);
		return c;
	}
}
