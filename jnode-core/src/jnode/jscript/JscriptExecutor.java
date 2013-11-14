package jnode.jscript;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.script.*;

import jnode.dto.Schedule;
import jnode.dto.ScriptHelper;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.orm.ORMManager;

/**
 * Запускатель пользовательских скриптов по расписанию
 * 
 * @author Manjago
 * 
 */
public class JscriptExecutor implements Runnable {
	private static final String JSCRIPT_ENABLE = "jscript.enable";
	private static final long MILLISEC_IN_DAY = 86400000L;
	private static final Logger logger = Logger
			.getLogger(JscriptExecutor.class);

	public JscriptExecutor() {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.set(Calendar.DAY_OF_YEAR,
				calendar.get(Calendar.DAY_OF_YEAR));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 0);
		Date showDate = new Date(calendar.getTime().getTime() + MILLISEC_IN_DAY);
		Calendar now = Calendar.getInstance(Locale.US);
		long initialDelay = calendar.getTimeInMillis() - now.getTimeInMillis();
		if (initialDelay < 0) {
			initialDelay = 0;
		}

		logger.l3("First jscriptExecutor will run at " + showDate
				+ " and every 24h after");
		new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(this,
				initialDelay, MILLISEC_IN_DAY, TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("unchecked")
	private Bindings createBindings() {
		Bindings bindings = new SimpleBindings();
		for (ScriptHelper scriptHelper : ORMManager.INSTANSE
				.getScriptHelperDAO().getAll()) {
			Class<? super IJscriptHelper> clazz;
			try {
				clazz = (Class<? super IJscriptHelper>) Class
						.forName(scriptHelper.getClassName());
				IJscriptHelper object = (IJscriptHelper) clazz.newInstance();
				bindings.put(scriptHelper.getId(), object);
				logger.l4("ScriptHelper " + object.toString() + " ("
						+ scriptHelper.getId() + ") loaded");
			} catch (ClassNotFoundException e) {
				logger.l2("Helper " + scriptHelper.getClassName()
						+ " not found");
			} catch (InstantiationException e) {
				logger.l2("Helper " + scriptHelper.getClassName()
						+ " can't been initialized");
			} catch (IllegalAccessException e) {
				logger.l2("Helper " + scriptHelper.getClassName()
						+ " can't been initialized (2)");
			} catch (ClassCastException e) {
				logger.l2("Helper " + scriptHelper.getClassName()
						+ " is not IJscriptHelper");
			}

		}
		return bindings;
	}

	@Override
	public void run() {
		if (!MainHandler.getCurrentInstance().getBooleanProperty(
				JSCRIPT_ENABLE, true)) {
			return;
		}

		ScriptEngine engine = new ScriptEngineManager()
				.getEngineByName("javascript");
		Calendar now = Calendar.getInstance(Locale.US);
		List<Schedule> items = ORMManager.INSTANSE.getScheduleDAO().getAll();
		logger.l5(MessageFormat.format("{0} items in queue", items.size()));
        engine.setBindings(createBindings(), ScriptContext.ENGINE_SCOPE);
		for (Schedule item : items) {
			if (item.isNeedExec(now)) {
				try {
					executeScript(engine, item);
				} catch (ScriptException e) {
					logger.l2(MessageFormat.format("fail script {0} execution",
							item.getJscript().getId()), e);
				} catch (Exception e) {
					logger.l2(MessageFormat.format(
							"unexpected fail script {0} execution", item
									.getJscript().getId()), e);
				}
				logger.l5(MessageFormat.format("executed script {0}", item
						.getJscript().getId()));
			}
		}

	}

	private static void executeScript(ScriptEngine engine, Schedule item) throws ScriptException {
		if (item.getJscript() != null && item.getJscript().getId() != null) {

			String content = ORMManager.INSTANSE.getJscriptDAO()
					.getById(item.getJscript().getId()).getContent();
			if (content != null) {
                logger.l5(MessageFormat.format("execute script {0}", content));
                dump(engine);
				engine.eval(content);
			}

		}
	}

    private static void dump(ScriptEngine engine) throws ScriptException {
        if (!logger.isNeedLog5()){
            return;
        }

        Bindings bind = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        Set<String> allAttributes = bind.keySet();
        Set<String> allFunctions = new HashSet<String>();
        for ( String attr : allAttributes ) {
                allFunctions.add(engine.eval("typeof " + attr) + " " + attr);
        }
        logger.l5("engine atrrs " + Arrays.toString(allFunctions.toArray()));
    }

}
