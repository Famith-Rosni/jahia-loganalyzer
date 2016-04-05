package org.jahia.loganalyzer.analyzers.loglevel;

import org.jahia.loganalyzer.LogParserConfiguration;
import org.jahia.loganalyzer.analyzers.core.LineAnalyzerContext;
import org.jahia.loganalyzer.analyzers.core.WritingLineAnalyzer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A line analyzer to extract simple log lines above a specified log level
 * User: Serge Huber
 * Date: 9 juil. 2008
 * Time: 10:32:06
 */
public class StandardLogLineAnalyzer extends WritingLineAnalyzer {

    private static final org.apache.commons.logging.Log log =
            org.apache.commons.logging.LogFactory.getLog(StandardLogLineAnalyzer.class);
        
    Pattern standardLogPattern;
    private DateFormat dateFormat;
    private Map<String, StandardSummaryLogEntry> standardSummary = new TreeMap<String, StandardSummaryLogEntry>();
    private int standardMinimumLogLevel;

    public StandardLogLineAnalyzer(LogParserConfiguration logParserConfiguration) throws IOException {
        super(logParserConfiguration.getStandardDetailsOutputFile(), logParserConfiguration.getStandardSummaryOutputFile(), logParserConfiguration.getCsvSeparatorChar(), new StandardDetailsLogEntry(0, 0, null, null, null), new StandardSummaryLogEntry(0, 0, null, null, null), logParserConfiguration);
        standardLogPattern = Pattern.compile(logParserConfiguration.getStandardLogAnalyzerPattern());
        dateFormat = new SimpleDateFormat(logParserConfiguration.getDateFormatString());
        standardMinimumLogLevel = logParserConfiguration.getStandardMinimumLogLevel();
    }

    public String getKey() {
        return "loglevel";
    }

    public boolean isForThisAnalyzer(LineAnalyzerContext context) {
        Matcher matcher = standardLogPattern.matcher(context.getLine());
        boolean matches = matcher.matches();
        return matches;
    }

    public Date parseLine(LineAnalyzerContext context) throws IOException {
        Matcher matcher = standardLogPattern.matcher(context.getLine());
        boolean matches = matcher.matches();
        if (!matches) {
            return null;
        }
        String dateGroup = matcher.group(1);
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(dateGroup);
        } catch (ParseException e) {
            log.error("Error parsing date format in line " + context.getLine(), e);
        }
        StandardDetailsLogEntry detailsLogEntry = new StandardDetailsLogEntry(context.getLineNumber(), context.getLineNumber(), parsedDate, context.getJvmIdentifier(), context.getFile().getName());
        detailsLogEntry.setLevel(matcher.group(2));
        detailsLogEntry.setClassName(matcher.group(3));
        // group 4 is not used.
        detailsLogEntry.setMessage(matcher.group(5));

        if (detailsLogEntry.getLevelNumber() >= standardMinimumLogLevel) {
            writeDetails(detailsLogEntry);
        }

        StandardSummaryLogEntry standardSummaryLogEntry = standardSummary.get(Integer.toString(detailsLogEntry.getLevelNumber()) + ":" + detailsLogEntry.getMessage());
        if (standardSummaryLogEntry == null) {
            standardSummaryLogEntry = new StandardSummaryLogEntry(0, 0, parsedDate, context.getJvmIdentifier(), context.getFile().getName());
            standardSummaryLogEntry.setLevel(detailsLogEntry.getLevel());
            standardSummaryLogEntry.setLevelNumber(detailsLogEntry.getLevelNumber());
            standardSummaryLogEntry.setMessage(detailsLogEntry.getMessage());
        }
        standardSummaryLogEntry.incrementCount();
        standardSummary.put(Integer.toString(detailsLogEntry.getLevelNumber()) + ":" + detailsLogEntry.getMessage(), standardSummaryLogEntry);

        return parsedDate;
    }

    public void finishPreviousState(LineAnalyzerContext context) {
    }

    public void stop() throws IOException {
        for (StandardSummaryLogEntry standardSummaryLogEntry : standardSummary.values()) {
            writeSummary(standardSummaryLogEntry);
        }
        super.stop();
    }
}
