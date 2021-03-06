package org.jnode.httpd.routes.html;

import org.jnode.httpd.dto.PointRequest;

import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class PointRequestRoute extends Route {
	public PointRequestRoute() {
		super("/pointrequest");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String code = null;
		String node = req.queryParams("node");
		String point = req.queryParams("point");
		String fname = req.queryParams("fname");
		String lname = req.queryParams("lname");
		String email = req.queryParams("email");

		PointRequest pr = new PointRequest();
		// check node
		try {
			FtnAddress a = new FtnAddress(node);
			if (!MainHandler.getCurrentInstance().getInfo().getAddressList()
					.contains(a)) {
				code = "NOTNODE";
			}
		} catch (NumberFormatException e) {
			code = "NOTNODE";
		}
		if (code == null) {
			try {
				FtnAddress a = new FtnAddress(node + "." + point);
				if (null != FtnTools.getLinkByFtnAddress(a)) {
					code = "EXISTS";
				}
				pr.setAddr(a.toString());
			} catch (NumberFormatException e) {
				code = "NOTPOINT";
			}
		}
		if (code == null) {
			if (fname != null && lname != null && fname.length() > 3
					&& lname.length() >= 3) {
				pr.setName(fname + " " + lname);
			} else {
				code = "NOTNAME";
			}
		}
		if (code == null) {
			if (email != null
					&& email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
				pr.setEmail(email);
			} else {
				code = "NOTEMAIL";
			}
		}
		if (code == null) {
			String password = FtnTools.generate8d();
			pr.setPassword(password);
			synchronized (PointRequest.class) {
				if (null != ORMManager.get(PointRequest.class).getFirstAnd(
						"addr", "=", pr.getAddr())) {
					code = "EXISTS";
				} else {
					ORMManager.get(PointRequest.class).save(pr);
					writeNetmail(pr);
				}
			}
		}
		resp.header("Location", "/requestpointresult.html"
				+ ((code != null) ? "?code=" + code : ""));
		halt(302);
		return null;
	}

	private void writeNetmail(PointRequest pr) {
		String text = String
				.format("New point request:\n > Addr: %s\n > Name: %s\n > Email: %s\n > Password: %s\n",
						pr.getAddr(), pr.getName(), pr.getEmail(),
						pr.getPassword());
		FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
				FtnTools.getPrimaryFtnAddress(), MainHandler
						.getCurrentInstance().getInfo().getStationName(),
				MainHandler.getCurrentInstance().getInfo().getSysop(),
				"New point request", text);

	}
}
