package jnode.robot;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.dao.GenericRawResults;

import jnode.dto.*;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.orm.ORMManager;

/**
 * 
 * @author kreon
 * 
 */
public class AreaFix extends AbstractRobot {
	private static final Pattern LIST = Pattern.compile("^%LIST$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern QUERY = Pattern.compile("^%QUERY$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern ADD = Pattern.compile("^%?\\+?(\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern REM = Pattern.compile("^%?\\-(\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern RESCAN = Pattern.compile(
			"^%RESCAN (\\S+) (\\d+)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern ADD_RESCAN = Pattern.compile(
			"^%?\\+?(\\S+) /r=(\\d+)$", Pattern.CASE_INSENSITIVE);

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		Link link = getAndCheckLink(fmsg);
		if (link == null) {
			return;
		}

		StringBuilder reply = new StringBuilder();
		for (String line : fmsg.getText().split("\n")) {
			line = line.toLowerCase();
			if (HELP.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} help", getRobotName()),
						help());
			} else if (LIST.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} list", getRobotName()),
						list(link));
			} else if (QUERY.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} query", getRobotName()),
						query(link));
			} else {
				Matcher m = REM.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					reply.append(rem(link, area));
					continue;
				}
				m = ADD.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					reply.append(add(link, area));
					continue;
				}
				m = RESCAN.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					int num = Integer.valueOf(m.group(2));
					reply.append(rescan(link, area, num));
					continue;
				}
				m = ADD_RESCAN.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					int num = Integer.valueOf(m.group(2));
					reply.append(add(link, area));
					reply.append(rescan(link, area, num));
					continue;
				}
			}
		}
		if (reply.length() > 0) {
			FtnTools.writeReply(fmsg,
					MessageFormat.format("{0} reply", getRobotName()),
					reply.toString());
		}
	}

	/**
	 * Отправляем %HELP
	 * 
	 * @return
	 */
	protected String help() {
		return "Available commands:\n" + "%HELP - this message\n"
				+ "%LIST - list of available areas\n"
				+ "%QUERY - list of subscribed areas\n"
				+ "+echo.area - subscribe echo.area\n"
				+ "-echo.area - unsibscribe echo.area\n"
				+ "+echo.area /r=N - subscribe and rescan N messages\n"
				+ "%RESCAN echo.area N - rescan N messages";

	}

	/**
	 * Отправляем %LIST
	 * 
	 * @param link
	 * @return
	 * @throws SQLException
	 */
	private String list(Link link) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("Legend: * - subscribed\n\n========== List of echoareas ==========\n");
		String[] groups = FtnTools.getOptionStringArray(link,
				LinkOption.SARRAY_LINK_GROUPS);
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getOrderAnd(
				"name", true);
		for (Echoarea area : areas) {
			boolean denied = true;
			if (!"".equals(area.getGroup())) {
				for (String group : groups) {
					if (area.getGroup().equals(group)) {
						denied = false;
						break;
					}
				}
			} else {
				denied = false;
			}
			if (denied) {
				continue;
			}
			Subscription sub = ORMManager.get(Subscription.class).getFirstAnd(
					"echoarea_id", "=", area.getId(), "link_id", "=",
					link.getId());

			if (sub != null) {
				sb.append("* ");
			} else {
				sb.append("  ");
			}
			sb.append(area.getName());
			if (area.getDescription().length() > 0) {
				sb.append(" - ");
				sb.append(area.getDescription());
			}
			sb.append('\n');
		}
		sb.append("========== List of echoareas ==========\n");
		return sb.toString();

	}

	/**
	 * Отправляем %QUERY
	 * 
	 * @param link
	 * @return
	 * @throws SQLException
	 */
	private String query(Link link) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("========== List of subscribed areas ==========\n");
		GenericRawResults<String[]> echoes = ORMManager
				.get(Echoarea.class)
				.getRaw(String
						.format("SELECT a.name, a.description from echoarea a"
								+ " LEFT JOIN subscription s on (a.id=s.echoarea_id)"
								+ " WHERE s.link_id=%d ORDER BY a.name",
								link.getId()));
		for (String[] echo : echoes.getResults()) {
			sb.append("* ");
			sb.append(echo[0]);
			if (echo[1].length() > 0) {
				sb.append(" - ");
				sb.append(echo[1]);
			}
			sb.append('\n');
		}
		sb.append("========== List of subscribed areas ==========\n");
		return sb.toString();

	}

	private String add(Link link, String area) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		String[] grps = FtnTools.getOptionStringArray(link,
				LinkOption.SARRAY_LINK_GROUPS);
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			sb.append(area + " not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				Subscription sub = ORMManager.get(Subscription.class)
						.getFirstAnd("echoarea_id", "=", earea.getId(),
								"link_id", "=", link.getId());
				if (sub != null) {
					sb.append(" already subscribed");
				} else {
					boolean denied = true;
					if (!"".equals(earea.getGroup())) {
						for (String group : grps) {
							if (earea.getGroup().equals(group)) {
								denied = false;
								break;
							}
						}
					} else {
						denied = false;
					}
					if (denied) {
						sb.append(" access denied");
					} else {
						sub = new Subscription();
						sub.setArea(earea);
						sub.setLink(link);
						ORMManager.get(Subscription.class).save(sub);
						sb.append(" subscribed");
					}
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	private String rem(Link link, String area) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				Subscription sub = ORMManager.get(Subscription.class)
						.getFirstAnd("echoarea_id", "=", earea.getId(),
								"link_id", "=", link.getId());
				if (sub == null) {
					sb.append(" is not subscribed");
				} else {
					ORMManager.get(Subscription.class).delete("link_id", "=",
							link, "echoarea_id", "=", earea);
					sb.append(" unsubscribed");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	private String rescan(Link link, String area, int num) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				Subscription sub = ORMManager.get(Subscription.class)
						.getFirstAnd("echoarea_id", "=", earea.getId(),
								"link_id", "=", link.getId());
				if (sub == null) {
					sb.append(" is not subscribed");
				} else {
					List<Echomail> mails = ORMManager.get(Echomail.class)
							.getOrderLimitAnd(num, "id", false, "echoarea_id",
									"=", earea);
					for (int i = mails.size() - 1; i >= 0; --i) {
						Echomail mail = mails.get(i);
						ORMManager.get(EchomailAwaiting.class).save(
								new EchomailAwaiting(link, mail));
					}
					sb.append(" rescanned " + mails.size() + " messages");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	@Override
	protected String getRobotName() {
		return "AreaFix";
	}

	@Override
	protected boolean isEnabled(Link link) {
		return link != null
				&& FtnTools.getOptionBooleanDefTrue(link,
						LinkOption.BOOLEAN_AREAFIX);
	}

	@Override
	protected String getPasswordOption() {
		return LinkOption.STRING_AREAFIX_PWD;
	}

}
