package org.gitorious.jcryptionspring;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
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
			logger.debug("Processing crypted request");
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
		// Map<String, String> decryptedParameters;
		private Map<String, String[]> parameters = new LinkedHashMap<String, String[]>(
				16);

		public DecryptedParameterRequest(HttpServletRequest request,
				KeyPair keys) {
			super(request);
			this.keys = keys;

			String decrypted = JCryptionUtil.decrypt(
					request.getParameter(J_CRYPTION), keys);
			parameters = JCryptionUtil.parse(decrypted, "utf-8");
			if (logger.isDebugEnabled()) {
				for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
					logger.debug(entry.getKey() + ": "
							+ java.util.Arrays.toString(entry.getValue()));
				}
			}
		}

		@Override
		public String getParameter(String name) {
			Assert.notNull(name, "Parameter name must not be null");
			String[] arr = this.parameters.get(name);
			return (arr != null && arr.length > 0 ? arr[0] : null);
		}

		@Override
		public Map getParameterMap() {
			return Collections.unmodifiableMap(this.parameters);
		}

		@Override
		public Enumeration getParameterNames() {
			return Collections.enumeration(this.parameters.keySet());
		}

		@Override
		// TODO Still don't knwo how to size retVal
		public String[] getParameterValues(String name) {
			Assert.notNull(name, "Parameter name must not be null");
			return this.parameters.get(name);
		}
	}

}
