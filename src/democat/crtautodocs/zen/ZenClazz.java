package democat.crtautodocs.zen;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import democat.crtautodocs.Main;

public class ZenClazz {
	/** ZenGetters of the ZenClass */
	public Set<ZenGezzer> zenGetters = new HashSet<>();
	/** ZenSetters of the ZenClass */
	public Set<ZenSezzer> zenSetters = new HashSet<>();
	/** ZenMethods of the ZenClass */
	public Set<ZenMezzod> zenMethods = new HashSet<>();
	
	/** ZenScript import name */
	public String canonicalName;
	/** ZenScript class name */
	public String name;
	/** Java class name */
	public String internalName;
	/** Java classes that this class extends */
	public String[] extendNames;
	
	public ZenClazz(String name, String internalName) {
		this(name, internalName, "");
	}
	
	public ZenClazz(String name, String internalName, String... extendNames) {
		this.canonicalName = name;
		this.name = Main.getClassFromImport(name);
		this.internalName = internalName;
		this.extendNames = extendNames;
	}
	
	public void addExtends(String... e) {
		List<String> temp = new ArrayList<>(Arrays.asList(extendNames));
		temp.addAll(Arrays.asList(e));
		extendNames = temp.toArray(new String[0]);
	}
	
	public Map<String, ZenGezzer> getBaseGezzers() {
		return zenGetters.stream().collect(Collectors.toMap(ZenGezzer::getName, t -> t));
	}
	
	public Map<String, ZenGezzer> getInheritedGezzers() {
		Map<String, ZenGezzer> result = new HashMap<>();
		for (String e : extendNames) {
			if (!Main.internalClasses.containsKey(e)) continue;
			result.putAll(Main.internalClasses.get(e).getAllGezzers());
		}
		return result;
	}
	
	public Map<String, ZenGezzer> getOverridenGezzers() {
		Map<String, ZenGezzer> result = new HashMap<>();
		Map<String, ZenGezzer> base = getBaseGezzers();
		for (Map.Entry<String, ZenGezzer> e : getInheritedGezzers().entrySet()) {
			if (base.containsKey(e.getKey()))
				result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	public Map<String, ZenGezzer> getAllGezzers() {
		Map<String, ZenGezzer> result = getBaseGezzers();
		for (Map.Entry<String, ZenGezzer> e : getInheritedGezzers().entrySet()) {
			if (!result.containsKey(e.getKey())) 
				result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	public Map<String, ZenSezzer> getBaseSezzers() {
		return zenSetters.stream().collect(Collectors.toMap(ZenSezzer::getName, t -> t));
	}
	
	public Map<String, ZenSezzer> getInheritedSezzers() {
		Map<String, ZenSezzer> result = new HashMap<>();
		for (String e : extendNames) {
			if (!Main.internalClasses.containsKey(e)) continue;
			result.putAll(Main.internalClasses.get(e).getAllSezzers());
		}
		return result;
	}
	
	public Map<String, ZenSezzer> getOverridenSezzers() {
		Map<String, ZenSezzer> result = new HashMap<>();
		Map<String, ZenSezzer> base = getBaseSezzers();
		for (Map.Entry<String, ZenSezzer> e : getInheritedSezzers().entrySet()) {
			if (base.containsKey(e.getKey()))
				result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	public Map<String, ZenSezzer> getAllSezzers() {
		Map<String, ZenSezzer> result = getBaseSezzers();
		for (Map.Entry<String, ZenSezzer> e : getInheritedSezzers().entrySet()) {
			if (!result.containsKey(e.getKey())) 
				result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	public Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> getBaseMezzods() {
		return zenMethods.stream().collect(Collectors.toMap(t -> new SimpleEntry<>(t.getName(), t.params), t -> t));
	}
	
	public Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> getInheritedMezzods() {
		Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> result = new HashMap<>();
		for (String e : extendNames) {
			if (!Main.internalClasses.containsKey(e)) continue;
			result.putAll(Main.internalClasses.get(e).getAllMezzods());
		}
		return result;
	}
	
	public Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> getOverridenMezzods() {
		Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> result = new HashMap<>();
		Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> base = getBaseMezzods();
		for (Map.Entry<Map.Entry<String, ZenParazz[]>, ZenMezzod> e : getInheritedMezzods().entrySet()) {
			if (base.containsKey(e.getKey()))
				result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	public Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> getAllMezzods() {
		Map<Map.Entry<String, ZenParazz[]>, ZenMezzod> result = getBaseMezzods();
		for (Map.Entry<Map.Entry<String, ZenParazz[]>, ZenMezzod> e : getInheritedMezzods().entrySet()) {
			if (!result.containsKey(e.getKey()))
				result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	public boolean hasExtend() {
		return extendNames.length != 0;
	}
	
	public boolean merge(ZenClazz zc) {
		if (zc == null) return false;
		
		this.zenGetters.addAll(zc.zenGetters);
		this.zenSetters.addAll(zc.zenSetters);
		this.zenMethods.addAll(zc.zenMethods);
		
		this.addExtends(zc.extendNames);
		return true;
	}
	
	@Override
	public String toString() {
		return this.canonicalName + " (" + internalName + ")" + (hasExtend() ? " extends " + String.join(", ", extendNames) : "");
	}
}
