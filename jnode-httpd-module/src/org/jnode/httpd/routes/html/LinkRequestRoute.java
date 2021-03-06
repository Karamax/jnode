package org.jnode.httpd.routes.html;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.main.MainHandler;
import jnode.ndl.FtnNdlAddress;
import jnode.ndl.NodelistScanner;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.LinkRequest;

import spark.Request;
import spark.Response;
import spark.Route;

public class LinkRequestRoute extends Route {

	public LinkRequestRoute() {
		super("/linkrequest/:type");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String type = req.params(":type");
		String code = null;
		if (!"confirm".equals(type)) {
			String addr = req.queryParams("addr");
			String host = req.queryParams("host");
			String port = req.queryParams("port");
			if (addr != null && host != null && port != null) {
				LinkRequest lr = new LinkRequest();
				try {
					FtnAddress ftn = new FtnAddress(addr);
					if (ftn.getPoint() != 0) {
						code = "POINT";
					} else {
						FtnNdlAddress ndl = NodelistScanner.getInstance()
								.isExists(ftn);
						boolean exists = (ndl != null);
						if (!exists) {
							code = "NODELIST";
						} else {
							String name = (ndl.getLine() != null) ? ndl
									.getLine().split(",")[4].replace('_', ' ') : addr;
							lr.setName(name);
							lr.setAddress(addr);
						}
					}
				} catch (NumberFormatException e) {
					code = "FTN";
				}
				if (code == null) { // next step
					try {
						Integer iport = Integer.valueOf(port);
						if ("-".equals(host) || iport == 0) {
							host = "-";
							iport = 0;
						} else {
							Socket sock = new Socket();
							sock.connect(new InetSocketAddress(host, iport),
									10000);
							sock.close();
						}
						lr.setHost(host);
						lr.setPort(iport);
					} catch (IOException | NumberFormatException e) {
						code = "INET";
					}
				}
				if (code == null) { // next step
					synchronized (LinkRequest.class) {
						LinkRequest lr2 = ORMManager.get(LinkRequest.class)
								.getFirstAnd("address", "=", addr);
						if (lr2 == null) {
							String akey = FtnTools.generate8d();
							lr.setAkey(akey);
							ORMManager.get(LinkRequest.class).save(lr);
							writeKey(lr);
						} else {
							code = "EXISTS";
						}
					}
				}
			} else {
				code = "ERROR";
			}
		} else {
			String akey = req.queryParams("key");
			String id = req.queryParams("id");
			try {
				LinkRequest lr = ORMManager.get(LinkRequest.class).getById(id);
				if (lr != null && lr.getAkey().equals(akey)) { // valid
					String password = FtnTools.generate8d();
					Link link = new Link();
					link.setLinkName(lr.getName());
					link.setLinkAddress(lr.getAddress());
					link.setProtocolHost(lr.getHost());
					link.setProtocolPort(lr.getPort());
					link.setPaketPassword(password);
					link.setProtocolPassword(password);
					// check
					synchronized (Link.class) {
						Link l2 = ORMManager.get(Link.class).getFirstAnd(
								"ftn_address", "=", link.getLinkAddress());
						if (l2 == null) {
							writeGreets(link);
							ORMManager.get(Link.class).save(link);
							ORMManager.get(LinkRequest.class).delete(lr);
							code = "PASSWORD&password=" + password;
						} else {
							code = "EXISTS";
						}
					}
				} else {
					code = "NOKEY";
				}
			} catch (RuntimeException e) {
				code = "ERROR";
			}
		}
		resp.header("Location", "/requestlinkresult.html"
				+ ((code != null) ? "?code=" + code : ""));
		halt(302);
		return null;
	}

	private void writeGreets(Link link) {
		// написать нам на почту
		FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
				FtnTools.getPrimaryFtnAddress(), MainHandler
						.getCurrentInstance().getInfo().getStationName(),
				MainHandler.getCurrentInstance().getInfo().getSysop(),
				"New linkage", "New link with " + link.getLinkAddress()
						+ " completed");

		FtnTools.writeNetmail(
				FtnTools.getPrimaryFtnAddress(),
				new FtnAddress(link.getLinkAddress()),
				MainHandler.getCurrentInstance().getInfo().getStationName(),
				link.getLinkName(),
				"You are welcome",
				"You are now have linkage with our node.\n"
						+ "Please, follow the Fidonet rules and keep your connection stable\n"
						+ "Your password: " + link.getProtocolPassword(),
				FtnMessage.ATTR_CRASH, false);
	}

	private void writeKey(LinkRequest lr) {
		// написать нам на почту
		FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
				FtnTools.getPrimaryFtnAddress(), MainHandler
						.getCurrentInstance().getInfo().getStationName(),
				MainHandler.getCurrentInstance().getInfo().getSysop(),
				"New linkage request",
				"New link request from " + lr.getAddress() + " accepted");

		FtnTools.writeNetmail(
				FtnTools.getPrimaryFtnAddress(),
				new FtnAddress(lr.getAddress()),
				MainHandler.getCurrentInstance().getInfo().getStationName(),
				lr.getName(),
				"Link instructions",
				"Somebody have just started a linkage proccess from your address.\n"
						+ "If this was you, visit /confirmlink.html on our site and fill fields as described below:\n"
						+ " > Request Id: " + lr.getId() + "\n"
						+ " > Request Key: " + lr.getAkey() + "\n",
				FtnMessage.ATTR_CRASH, false);
	}

}
