package it.more.jcryption;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@XmlRootElement
public class PublicKeyImpl {
	String e;
	String n;
	String maxdigits;
}
