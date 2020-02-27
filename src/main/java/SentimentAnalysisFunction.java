/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.asterix.external.api.IExternalScalarFunction;
import org.apache.asterix.external.api.IFunctionHelper;
import org.apache.asterix.external.library.java.base.JInt;
import org.apache.asterix.external.library.java.base.JString;
import org.apache.asterix.external.library.java.base.JRecord;
import org.apache.asterix.external.library.java.JTypeTag;

import java.util.Properties;

public class SentimentAnalysisFunction implements IExternalScalarFunction {

    private static StanfordCoreNLP pipeline;

    @Override
    public void deinitialize() {
        System.out.println("De-Initialized");
    }

    @Override
    public void evaluate(IFunctionHelper functionHelper) throws Exception {

        //-------------Apply with twitter data----------------//
        JRecord inputRecord = (JRecord) functionHelper.getArgument(0);
        JString text = (JString) inputRecord.getValueByName("text");
        //----------------------------------------------------//

        //-------------Apply to general text------------------//
        // JString text = ((JString) functionHelper.getArgument(0));
        //----------------------------------------------------//

        JRecord record = (JRecord) functionHelper.getResultObject();

        JInt num = (JInt) functionHelper.getObject(JTypeTag.INT);
        JString sentimentText = (JString) functionHelper.getObject(JTypeTag.STRING);

        //Getting sentiment score
        int score = findSentiment(text.getValue());

        num.setValue(score);

        //Sentiment types
        String[] sentimentType = { "Very Negative","Negative", "Neutral", "Positive", "Very Positive"};
        String sentiment = sentimentType[score];


        sentimentText.setValue(sentiment);


        record.setField("id", inputRecord.getValueByName("id"));
        record.setField("text", text);
        record.setField("score", num);
        record.setField("sentiment", sentimentText);

        functionHelper.setResult(record);
    }

    @Override
    public void initialize(IFunctionHelper functionHelper) throws Exception {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public static int findSentiment(String tweet) {

        int mainSentiment = 0;
        if (tweet != null && tweet.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(tweet);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        return mainSentiment;
    }

}
