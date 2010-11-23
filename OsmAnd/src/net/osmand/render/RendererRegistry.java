package net.osmand.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.osmand.LogUtil;

import org.apache.commons.logging.Log;
import org.xml.sax.SAXException;


public class RendererRegistry {

	private final static RendererRegistry registry = new RendererRegistry();
	private final static Log log = LogUtil.getLog(RendererRegistry.class);
	
	public final static String DEFAULT_RENDER = "default";  //$NON-NLS-1$
	public final static String NIGHT_SUFFIX = "-night"; //$NON-NLS-1$
	public final static String DEFAULT_NIGHT_RENDER = DEFAULT_RENDER + NIGHT_SUFFIX; 
	
	public static RendererRegistry getRegistry() {
		return registry;
	}
	
	public RendererRegistry(){
		internalRenderers.put(DEFAULT_RENDER, "default.render.xml"); //$NON-NLS-1$
		internalRenderers.put(DEFAULT_NIGHT_RENDER, "default-night.render.xml"); //$NON-NLS-1$
	}
	
	private BaseOsmandRender defaultRender = null;
	private BaseOsmandRender currentSelectedRender = null;
	
	private Map<String, File> externalRenderers = new LinkedHashMap<String, File>();
	private Map<String, String> internalRenderers = new LinkedHashMap<String, String>();
	
	private Map<String, BaseOsmandRender> renderers = new LinkedHashMap<String, BaseOsmandRender>(); 
	
	public BaseOsmandRender defaultRender() {
		if(defaultRender == null){
			defaultRender = getRenderer(DEFAULT_RENDER);
			if (defaultRender == null) {
				try {
					defaultRender = new BaseOsmandRender();
					defaultRender.init(OsmandRenderingRulesParser.class.getResourceAsStream("default.render.xml")); //$NON-NLS-1$
				} catch (IOException e) {
					log.error("Exception initialize renderer", e); //$NON-NLS-1$
				} catch (SAXException e) {
					log.error("Exception initialize renderer", e); //$NON-NLS-1$
				}
			}
		}
		return defaultRender;
	}
	
	public BaseOsmandRender getRenderer(String name){
		if(renderers.containsKey(name)){
			return renderers.get(name);
		}
		if(!externalRenderers.containsKey(name) && !internalRenderers.containsKey(name)){
			return null;
		}
		return getRenderer(name, new LinkedHashSet<String>());
	}
	
	private BaseOsmandRender getRenderer(String name, Set<String> loadedRenderers) {
		try {
			return loadRenderer(name);
		} catch (IOException e) {
			log.error("Error loading renderer", e); //$NON-NLS-1$
		} catch (SAXException e) {
			log.error("Error loading renderer", e); //$NON-NLS-1$
		}
		return null;
	}
	
	public BaseOsmandRender loadRenderer(String name) throws IOException, SAXException {
		return loadRenderer(name, new LinkedHashSet<String>());
	}
	
	private BaseOsmandRender loadRenderer(String name, Set<String> loadedRenderers) throws IOException, SAXException {
		InputStream is = null;
		if(externalRenderers.containsKey(name)){
			is = new FileInputStream(externalRenderers.get(name));
		} else if(internalRenderers.containsKey(name)){
			is = OsmandRenderingRulesParser.class.getResourceAsStream("default.render.xml"); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException("Not found " + name); //$NON-NLS-1$
		}
		BaseOsmandRender b = new BaseOsmandRender();
		b.init(is);
		loadedRenderers.add(name);
		List<BaseOsmandRender> dependencies = new ArrayList<BaseOsmandRender>();
		for (String s : b.getDepends()) {
			if (loadedRenderers.contains(s)) {
				log.warn("Circular dependencies found " + name); //$NON-NLS-1$
			} else {
				BaseOsmandRender dep = getRenderer(s, loadedRenderers);
				if (dep == null) {
					log.warn("Dependent renderer not found : "  + name); //$NON-NLS-1$
				} else{
					dependencies.add(dep);
				}
			}
		}
		b.setDependRenderers(dependencies);
		renderers.put(name, b);
		return b;
	}
	
	
	public void setExternalRenderers(Map<String, File> externalRenderers) {
		this.externalRenderers = externalRenderers;
	}
	
	public Collection<String> getRendererNames(){
		LinkedHashSet<String> names = new LinkedHashSet<String>();
		names.add(DEFAULT_RENDER);
		names.addAll(internalRenderers.keySet());
		names.addAll(externalRenderers.keySet());
		return names;
	}

	public BaseOsmandRender getCurrentSelectedRenderer() {
		if(currentSelectedRender == null){
			return defaultRender();
		}
		return currentSelectedRender;
	}
	
	public void setCurrentSelectedRender(BaseOsmandRender currentSelectedRender) {
		this.currentSelectedRender = currentSelectedRender;
	}
	
}