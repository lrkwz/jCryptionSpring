package it.more.jcryption;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.linkwithweb.encryption.JCryptionUtil;

public class ParameterDecryptingFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory
			.getLogger(ParameterDecryptingFilter.class);

	private static final String J_CRYPTION = "jCryption";

	public void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		HttpServletRequestWrapper wrappedRequest = null;
		if (request.getParameter(J_CRYPTION) != null) {
			KeyPair keys = (KeyPair) request.getSession().getAttribute("keys");
			if (keys != null) {
				wrappedRequest = new DecryptedParameterRequest(request, keys);
			} else {
				logger.debug("No keys in session");
			}
		}
		filterChain.doFilter(wrappedRequest != null ? wrappedRequest : request,
				response);
	}

	class DecryptedParameterRequest extends HttpServletRequestWrapper {

		KeyPair keys;
		Map<String, String> decryptedParameters;

		public DecryptedParameterRequest(HttpServletRequest request,
				KeyPair keys) {
			super(request);
			this.keys = keys;

			String decrypted = JCryptionUtil.decrypt(
					request.getParameter(J_CRYPTION), keys);
			decryptedParameters = JCryptionUtil.parse(decrypted, null);
		}

		@Override
		public String getParameter(String name) {
			return decryptedParameters.get(name);
		}

		@Override
		public Map getParameterMap() {
			return decryptedParameters;
		}

		@Override
		public Enumeration getParameterNames() {
			return new IteratorEnumeration(decryptedParameters.keySet()
					.iterator());

		}

		@Override
		// TODO verificare!
		public String[] getParameterValues(String name) {
			String[] retVal = { "" };
			retVal[0] = decryptedParameters.get(name);
			return retVal;
		}
	}

}
