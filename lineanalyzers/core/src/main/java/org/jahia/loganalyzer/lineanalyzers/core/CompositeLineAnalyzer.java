package org.jahia.loganalyzer.lineanalyzers.core;

/*
 * #%L
 * Jahia Log Analyzer
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2007 - 2016 Jahia
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.jahia.loganalyzer.api.LineAnalyzer;
import org.jahia.loganalyzer.api.LineAnalyzerContext;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * A line analyzer that delegates to an array of line analyzers
 * User: Serge Huber
 * Date: July 8th, 2008
 * Time: 10:31:22
 */
public class CompositeLineAnalyzer implements LineAnalyzer {

    List<LineAnalyzer> lineAnalyzers;
    LineAnalyzer currentlyActiveAnalyzer = null;

    public CompositeLineAnalyzer(List<LineAnalyzer> lineAnalyzers) {
        this.lineAnalyzers = lineAnalyzers;        
    }

    public String getKey() {
        return "composite";
    }

    public boolean isForThisAnalyzer(LineAnalyzerContext context) {
        for (LineAnalyzer lineAnalyzer : lineAnalyzers) {
            if (lineAnalyzer.isForThisAnalyzer(context)) {
                if (lineAnalyzer != currentlyActiveAnalyzer) {
                    if (currentlyActiveAnalyzer != null) {
                        currentlyActiveAnalyzer.finishPreviousState(context);
                    }
                    currentlyActiveAnalyzer = lineAnalyzer;
                }
                return true;
            }
        }
        return false;
    }

    public Date parseLine(LineAnalyzerContext context) throws IOException {
        if (isForThisAnalyzer(context)) {
            return currentlyActiveAnalyzer.parseLine(context);
        }
        return null;
    }

    public void finishPreviousState(LineAnalyzerContext context) {
        for (LineAnalyzer lineAnalyzer : lineAnalyzers) {
            lineAnalyzer.finishPreviousState(context);
        }
    }

    public void stop() throws IOException {
        for (LineAnalyzer lineAnalyzer : lineAnalyzers) {
            lineAnalyzer.stop();
        }
    }
}
