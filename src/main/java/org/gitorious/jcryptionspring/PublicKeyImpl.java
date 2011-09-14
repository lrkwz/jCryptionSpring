package org.gitorious.jcryptionspring;


//@XmlRootElement
public class PublicKeyImpl {
	String e;
	String n;
	String maxdigits;

	public String getE() {
		return e;
	}

	public void setE(String e) {
		this.e = e;
	}

	public String getN() {
		return n;
	}

	public void setN(String n) {
		this.n = n;
	}

	public String getMaxdigits() {
		return maxdigits;
	}

	public void setMaxdigits(String maxdigits) {
		this.maxdigits = maxdigits;
	}

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("E: ").append(getE()).append(", ");
        sb.append("Maxdigits: ").append(getMaxdigits()).append(", ");
        sb.append("N: ").append(getN());
        return sb.toString();
    }

}
