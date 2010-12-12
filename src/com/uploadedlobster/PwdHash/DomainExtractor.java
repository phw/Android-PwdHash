/**
 * 
 */
package com.uploadedlobster.PwdHash;

/**
 * Domain name extractor.
 * 
 * Turns host names into domain names. Based on original JavaScript code from:
 * https://www.pwdhash.com/
 * 
 * @author Philipp Wolfer <ph.wolfer@googlemail.com>
 */
public final class DomainExtractor {

	public static String extractDomain(String uri) throws IllegalArgumentException {
		if (uri == null)
			throw new IllegalArgumentException("Argument uri must not be null.");
		
		String domain = uri;

		domain = domain.replace("http://", "");
		domain = domain.replace("https://", "");

		int firstSlash;
		if ((firstSlash = domain.indexOf("/")) > -1)
			domain = domain.substring(0, firstSlash);

		String[] parts = domain.split("\\.");

		if (parts.length > 2) {
			domain = parts[parts.length - 2] + "." + parts[parts.length - 1];

			for (String sld : mSecondLevelDomains) {
				if (domain.equals(sld))
					domain = parts[parts.length - 3] + "." + domain;
			}
		}

		return domain;
	}

	private static String[] mSecondLevelDomains = { "ab.ca", "ac.ac", "ac.at",
			"ac.be", "ac.cn", "ac.il", "ac.in", "ac.jp", "ac.kr", "ac.nz",
			"ac.th", "ac.uk", "ac.za", "adm.br", "adv.br", "agro.pl", "ah.cn",
			"aid.pl", "alt.za", "am.br", "arq.br", "art.br", "arts.ro",
			"asn.au", "asso.fr", "asso.mc", "atm.pl", "auto.pl", "bbs.tr",
			"bc.ca", "bio.br", "biz.pl", "bj.cn", "br.com", "cn.com", "cng.br",
			"cnt.br", "co.ac", "co.at", "co.il", "co.in", "co.jp", "co.kr",
			"co.nz", "co.th", "co.uk", "co.za", "com.au", "com.br", "com.cn",
			"com.ec", "com.fr", "com.hk", "com.mm", "com.mx", "com.pl",
			"com.ro", "com.ru", "com.sg", "com.tr", "com.tw", "cq.cn",
			"cri.nz", "de.com", "ecn.br", "edu.au", "edu.cn", "edu.hk",
			"edu.mm", "edu.mx", "edu.pl", "edu.tr", "edu.za", "eng.br",
			"ernet.in", "esp.br", "etc.br", "eti.br", "eu.com", "eu.lv",
			"fin.ec", "firm.ro", "fm.br", "fot.br", "fst.br", "g12.br",
			"gb.com", "gb.net", "gd.cn", "gen.nz", "gmina.pl", "go.jp",
			"go.kr", "go.th", "gob.mx", "gov.br", "gov.cn", "gov.ec", "gov.il",
			"gov.in", "gov.mm", "gov.mx", "gov.sg", "gov.tr", "gov.za",
			"govt.nz", "gs.cn", "gsm.pl", "gv.ac", "gv.at", "gx.cn", "gz.cn",
			"hb.cn", "he.cn", "hi.cn", "hk.cn", "hl.cn", "hn.cn", "hu.com",
			"idv.tw", "ind.br", "inf.br", "info.pl", "info.ro", "iwi.nz",
			"jl.cn", "jor.br", "jpn.com", "js.cn", "k12.il", "k12.tr",
			"lel.br", "ln.cn", "ltd.uk", "mail.pl", "maori.nz", "mb.ca",
			"me.uk", "med.br", "med.ec", "media.pl", "mi.th", "miasta.pl",
			"mil.br", "mil.ec", "mil.nz", "mil.pl", "mil.tr", "mil.za",
			"mo.cn", "muni.il", "nb.ca", "ne.jp", "ne.kr", "net.au", "net.br",
			"net.cn", "net.ec", "net.hk", "net.il", "net.in", "net.mm",
			"net.mx", "net.nz", "net.pl", "net.ru", "net.sg", "net.th",
			"net.tr", "net.tw", "net.za", "nf.ca", "ngo.za", "nm.cn", "nm.kr",
			"no.com", "nom.br", "nom.pl", "nom.ro", "nom.za", "ns.ca", "nt.ca",
			"nt.ro", "ntr.br", "nx.cn", "odo.br", "on.ca", "or.ac", "or.at",
			"or.jp", "or.kr", "or.th", "org.au", "org.br", "org.cn", "org.ec",
			"org.hk", "org.il", "org.mm", "org.mx", "org.nz", "org.pl",
			"org.ro", "org.ru", "org.sg", "org.tr", "org.tw", "org.uk",
			"org.za", "pc.pl", "pe.ca", "plc.uk", "ppg.br", "presse.fr",
			"priv.pl", "pro.br", "psc.br", "psi.br", "qc.ca", "qc.com",
			"qh.cn", "re.kr", "realestate.pl", "rec.br", "rec.ro", "rel.pl",
			"res.in", "ru.com", "sa.com", "sc.cn", "school.nz", "school.za",
			"se.com", "se.net", "sh.cn", "shop.pl", "sk.ca", "sklep.pl",
			"slg.br", "sn.cn", "sos.pl", "store.ro", "targi.pl", "tj.cn",
			"tm.fr", "tm.mc", "tm.pl", "tm.ro", "tm.za", "tmp.br",
			"tourism.pl", "travel.pl", "tur.br", "turystyka.pl", "tv.br",
			"tw.cn", "uk.co", "uk.com", "uk.net", "us.com", "uy.com", "vet.br",
			"web.za", "web.com", "www.ro", "xj.cn", "xz.cn", "yk.ca", "yn.cn",
			"za.com" };
}
