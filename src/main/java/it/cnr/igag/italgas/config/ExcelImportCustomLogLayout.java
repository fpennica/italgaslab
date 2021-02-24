package it.cnr.igag.italgas.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class ExcelImportCustomLogLayout extends LayoutBase<ILoggingEvent> {

	@Override
	public String doLayout(ILoggingEvent event) {
		StringBuffer sbuf = new StringBuffer(128);

		if ((event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN)) {
			
			sbuf.append("---");
			sbuf.append(CoreConstants.LINE_SEPARATOR);
			sbuf.append(CoreConstants.LINE_SEPARATOR);
			if (event.getLevel() == Level.ERROR)
				sbuf.append("**ERRORE**");
			if (event.getLevel() == Level.WARN)
				sbuf.append("**ATTENZIONE**");
			sbuf.append(CoreConstants.LINE_SEPARATOR);
		}
		
		if ((event.getLevel() == Level.DEBUG) || event.getLevel() == Level.TRACE) {
			sbuf.append("`");
			sbuf.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(event.getTimeStamp())));
			sbuf.append(" - ");
		}
		
		sbuf.append(event.getFormattedMessage());
		
		if ((event.getLevel() == Level.DEBUG) || event.getLevel() == Level.TRACE) {
			sbuf.append("`");
		}
		
		sbuf.append(CoreConstants.LINE_SEPARATOR);
		sbuf.append(CoreConstants.LINE_SEPARATOR);

		if ((event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN) && event.getThrowableProxy() != null) {
			sbuf.append("```");
			sbuf.append(StringUtils.substringBefore(event.getThrowableProxy().getMessage(), IOUtils.LINE_SEPARATOR));
			sbuf.append("```");
			sbuf.append(CoreConstants.LINE_SEPARATOR);
		}
		
		
		if ((event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN)) {
			sbuf.append("---");
			sbuf.append(CoreConstants.LINE_SEPARATOR);
			sbuf.append(CoreConstants.LINE_SEPARATOR);
		}
		
		return sbuf.toString();
	}

}
