package org.space.hulu.config;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.space.hulu.exception.MissingPropertyException;
import org.space.hulu.util.Validation;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * this cofig is for the common config for both client and server.
 *
 * @author Denny Ye
 * @since 2012-5-19
 * @version 1.0
 */
public class CommonConfig {
	private static final Log LOG = LogFactory.getLog(CommonConfig.class);

	private static final String DEFAULT_CONF_NAME = "hulu.xml";
	
	private Properties properties;
	
	private volatile boolean hasInited = false;

	private static CommonConfig instance = new CommonConfig();
	
	public static CommonConfig getInstance(){
		return instance;
	}
	
	private CommonConfig(){}
	
	/**
	 * Obtains String configuration
	 * 
	 * @param key parameter key
	 * @param defaultValue user defined
	 * @return
	 */
	public String get(String key, String defaultValue) {
		ensureInited();
		return properties.containsKey(key) ? properties.getProperty(key) : defaultValue;
	}
	
	/**
	 * Exception when key is missing
	 * 
	 * @param key
	 * @return
	 */
	public String getOrException(String key) {
		ensureInited();
		
		if (properties.containsKey(key)) {
			return properties.getProperty(key);
		} 
		
		throw new MissingPropertyException(key);
	}
	
	/**
	 * Obtains Boolean configuration
	 * 
	 * @param key parameter key
	 * @param defaultValue user defined
	 * @return
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		ensureInited();
		
		boolean fResult = defaultValue;
		String value = properties.getProperty(key);
		if (value != null) {
			fResult = Boolean.parseBoolean(value);
		}
		
		return fResult;
	}
	
	/**
	 * Obtains setting in integer
	 * 
	 * @param key parameter key
	 * @param defaultValue user defined
	 * @return
	 */
	public int getInt(String key, int defaultValue) {
		ensureInited();
		
		int fResult = defaultValue;
		String value = properties.getProperty(key);
		if (value != null) {
			try {
				fResult = Integer.parseInt(value);
			} catch (NumberFormatException e) {}
		}
		
		return fResult;
	}
	
	/**
	 * Obtains value or throws exception
	 * 
	 * @param key
	 * @return
	 */
	public int getIntOrException(String key) {
		ensureInited();
		
		String value = properties.getProperty(key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {}
		}
		
		throw new MissingPropertyException(key);
	}
	
	/**
	 * Obtains setting in long
	 * 
	 * @param key parameter key
	 * @param defaultValue user defined
	 * @return
	 */
	public long getLong(String key, long defaultValue) {
		ensureInited();
		
		long fResult = defaultValue;
		String value = properties.getProperty(key);
		if (value != null) {
			try {
				fResult = Long.parseLong(value);
			} catch (NumberFormatException e) {}
		}
		
		return fResult;
	}
	
	public void set(String key, String value) {
		ensureInited();
		
		Validation.effectiveStr(key);
		Validation.effectiveStr(value);
		
		properties.setProperty(key, value);
	}
	
	public void set(String key, int value) {
		ensureInited();
		
		Validation.effectiveStr(key);
		
		properties.setProperty(key, String.valueOf(value));
	}
	
	private void ensureInited() {
		if (!hasInited) {
			synchronized (CommonConfig.class) {
				if (!hasInited) {
					properties = new Properties();
					try {
						loadResource(properties, DEFAULT_CONF_NAME);
					} catch (Exception e) {
						LOG.error("Configuration initialized failed. cause:" 
														+ e.getMessage());
					}
					
					hasInited = true;
				}
			}
		}
	}

	private void loadResource(Properties properties, Object name) {
		try {
			DocumentBuilderFactory docBuilderFactory
						= DocumentBuilderFactory.newInstance();
			//ignore all comments inside the xml file
			docBuilderFactory.setIgnoringComments(true);

			//allow includes in the xml file
			docBuilderFactory.setNamespaceAware(true);
			try {
				docBuilderFactory.setXIncludeAware(true);
			} catch (UnsupportedOperationException e) {
				LOG.error("Failed to set setXIncludeAware(true) for parser "
						+ docBuilderFactory
						+ ":" + e,
						e);
			}

			DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
			Document doc = null;
			Element root = null;

			

			if (name instanceof String) {        // a CLASSPATH resource
				URL url = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_CONF_NAME);
				if (url != null) {
					doc = builder.parse(url.toString());
				}
			} else if (name instanceof Element) {
			    root = (Element)name;
			}
			
			if (doc == null && root == null) {
				throw new RuntimeException(name + " not found");
			}
	
			if (root == null) {
				root = doc.getDocumentElement();
			}
			
			if (!"configuration".equals(root.getTagName())) {
				LOG.fatal("bad conf file: top-level element not <configuration>");
			}
			
			NodeList props = root.getChildNodes();
			for (int i = 0; i < props.getLength(); i++) {
				Node propNode = props.item(i);
				if (!(propNode instanceof Element)) {
					continue;
				}
				
				Element prop = (Element)propNode;
				if ("configuration".equals(prop.getTagName())) {
					loadResource(properties, prop);
					continue;
				}
				
				if (!"property".equals(prop.getTagName())) {
					LOG.warn("bad conf file: element not <property>");
				}
				
				NodeList fields = prop.getChildNodes();
				String attr = null;
				String value = null;
				for (int j = 0; j < fields.getLength(); j++) {
					Node fieldNode = fields.item(j);
					if (!(fieldNode instanceof Element)) {
						continue;
					}
					
					Element field = (Element)fieldNode;
					if ("name".equals(field.getTagName()) && field.hasChildNodes()) {
						attr = ((Text)field.getFirstChild()).getData().trim();
					}
					if ("value".equals(field.getTagName()) && field.hasChildNodes()) {
						value = ((Text)field.getFirstChild()).getData();
					}

					if (attr != null) {
						if (value != null) {
							properties.setProperty(attr, value);
						}
					}
					
				}
			}
		} catch (IOException e) {
			LOG.fatal("error parsing conf file: " + e);
			throw new RuntimeException(e);
		} catch (DOMException e) {
			LOG.fatal("error parsing conf file: " + e);
			throw new RuntimeException(e);
		} catch (SAXException e) {
			LOG.fatal("error parsing conf file: " + e);
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			LOG.fatal("error parsing conf file: " + e);
			throw new RuntimeException(e);
		}
	}
}

