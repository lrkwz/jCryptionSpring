package it.more.jcryption;

import java.security.KeyPair;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.linkwithweb.encryption.JCryptionUtil;

@Controller
@SessionAttributes("keys")
public class EncryptionController {
	private static final Logger logger = LoggerFactory
			.getLogger(EncryptionController.class);

	@RequestMapping("/EncryptionServlet")
	public @ResponseBody
	PublicKeyImpl getPublicKey(
			@RequestParam(required = false) Boolean generateKeypair,
			HttpSession session) {

		PublicKeyImpl retVal = null;

		if (generateKeypair != null && generateKeypair == true) {

			JCryptionUtil jCryptionUtil = new JCryptionUtil();

			KeyPair keys = null;
			if (session.getAttribute("keys") == null) {
				keys = jCryptionUtil.generateKeypair(512);
				session.setAttribute("keys", keys);
			} else {
				keys = (KeyPair) session.getAttribute("keys");
			}

			retVal = new PublicKeyImpl();
			retVal.setE(JCryptionUtil.getPublicKeyExponent(keys));
			retVal.setN(JCryptionUtil.getPublicKeyModulus(keys));
			retVal.setMaxdigits(String.valueOf(JCryptionUtil.getMaxDigits(512)));

			logger.debug("Public key is :" + retVal);
		}
		return retVal;
	}

}
