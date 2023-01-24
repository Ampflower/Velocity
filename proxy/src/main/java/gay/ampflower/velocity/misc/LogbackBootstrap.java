package gay.ampflower.velocity.misc;// Created 2022-19-07T09:58:33

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.util.HashMap;
import java.util.Map;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import net.minecrell.terminalconsole.util.LogbackClassicNameExtractor;
import net.minecrell.terminalconsole.util.LoggerNameLayoutSelector;

/**
 * @author Ampflower
 * @since ${version}
 **/
public class LogbackBootstrap extends ContextAwareBase implements Configurator {
  @Override
  public void configure(LoggerContext loggerContext) {
    addInfo("Starting up Velocity logger...");

    Map<String, String> ruleRegistry = (Map<String, String>) loggerContext.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
    if (ruleRegistry == null) {
      ruleRegistry = new HashMap<>();
      context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);
    }

    ruleRegistry.put("minecraftFormatting", "net.minecrell.terminalconsole.MinecraftFormattingConverter");
    ruleRegistry.put("highlightError", "net.minecrell.terminalconsole.HighlightErrorConverter");

    TerminalConsoleAppender<ILoggingEvent> tca = new TerminalConsoleAppender<>();
    tca.setContext(loggerContext);

    var defaultPattern = new PatternLayout();
    defaultPattern.setContext(loggerContext);
    defaultPattern.setPattern("%highlightError([%d{HH:mm:ss} %level] [%logger{35}]: %minecraftFormatting(%msg){}%n%ex)");
    defaultPattern.start();

    var lnls = new LoggerNameLayoutSelector<>(loggerContext, defaultPattern, new LogbackClassicNameExtractor());

    var velocityPattern = new PatternLayout();
    velocityPattern.setContext(loggerContext);
    velocityPattern.setPattern("%highlightError([%d{HH:mm:ss} %level]: %minecraftFormatting(%msg){}%n%ex)");
    velocityPattern.start();

    lnls.addFormatter("com.velocitypowered.", velocityPattern);
    lnls.start();

    tca.setLayout(lnls);
    tca.start();

    var root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    root.addAppender(tca);
  }

}
