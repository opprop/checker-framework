package org.checkerframework.dataflow.cfg.playground;

import org.checkerframework.dataflow.analysis.BackwardAnalysis;
import org.checkerframework.dataflow.analysis.BackwardAnalysisImpl;
import org.checkerframework.dataflow.cfg.CFGVisualizeLauncher;
import org.checkerframework.dataflow.livevariable.LiveVar;
import org.checkerframework.dataflow.livevariable.LiveVariableStore;
import org.checkerframework.dataflow.livevariable.LiveVariableTransfer;

public class LiveVariablePlayground {
    /**
     * Run live variable for a specific file and create a PDF of the CFG
     * in the end.
     */
    public static void main(String[] args) {

        /* Configuration: change as appropriate */
        String inputFile = "Test.java"; // input file name and path
        String outputDir = "cfg"; // output directory
        String method = "test"; // name of the method to analyze
        String clazz = "Test"; // name of the class to consider

        // run the analysis and create a PDF file
        LiveVariableTransfer transfer = new LiveVariableTransfer();
        // TODO: correct processing environment
        BackwardAnalysis<LiveVar, LiveVariableStore, LiveVariableTransfer> forwardAnalysis =
                new BackwardAnalysisImpl<>(transfer);
        CFGVisualizeLauncher.generateDOTofCFG(
                inputFile, outputDir, method, clazz, false, forwardAnalysis);
    }
}
