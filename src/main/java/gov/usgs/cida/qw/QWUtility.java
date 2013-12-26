package gov.usgs.cida.qw;

import gov.usgs.cida.resourcefolder.Response;

import java.util.Map.Entry;
import java.util.TreeMap;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

public class QWUtility {

	public static String PROVIDERS_XML;
	
	public static TreeMap<String, String> PROVIDERS_MAP = new TreeMap<>();
	
	static {
		//Build the list of providers off the jndi context.
		try {
			NamingEnumeration<Binding> providers = new InitialContext().listBindings("java:comp/env/WQP/providers");
			while (providers.hasMore()) {
				Binding provider = providers.next();
				PROVIDERS_MAP.put(provider.getName(), provider.getObject().toString());
			}
			StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><providers>");
			for (Entry<String, String> provider : PROVIDERS_MAP.entrySet()) {
				sb.append("<provider>").append(provider.getKey()).append("</provider>");
			}
			PROVIDERS_XML = sb.append("</providers>").toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("java:comp/env/WQP/providers not configured correctly.");
		}
	}
	
	/**
	 * Given a response, compare it's responsible end point to the list of providers and return the name of the provider.
	 * @param response - the Resource Folder response.
	 * @return the common name of the provider.
	 */
	public static String determineProvider(final Response response) {
		String providerName = "unknown";
		if (null != response && null != response.getResponsibleEndpoint() && 0 < response.getResponsibleEndpoint().toString().length()) {
			for (Entry<String, String> entry : QWUtility.PROVIDERS_MAP.entrySet()) {
				if (response.getResponsibleEndpoint().toString().startsWith(entry.getValue().substring(0, entry.getValue().lastIndexOf("/")))) {
					providerName = entry.getKey();
					break; //stop looping when we get a hit.
				}
			}
		}
		return providerName;
	}

}