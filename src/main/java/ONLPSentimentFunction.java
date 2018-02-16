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



import org.apache.asterix.external.api.IExternalScalarFunction;
import org.apache.asterix.external.api.IFunctionHelper;
import org.apache.asterix.external.library.java.JObjects.JInt;
import org.apache.asterix.external.library.java.JObjects.JRecord;
import org.apache.asterix.external.library.java.JObjects.JString;
import org.apache.asterix.external.library.java.JTypeTag;


import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ONLPSentimentFunction implements IExternalScalarFunction {

    private static DoccatModel m;

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
        int score = getSentiment(text.getValue(), m);

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
//        InputStream is = new FileInputStream("output/en-doccat.bin");
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("en-doccat.bin");
        m = new DoccatModel(in);
    }


    public static int getSentiment(String tweet, DoccatModel model) {
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
        double[] outcomes = myCategorizer.categorize(tweet);
        String category = myCategorizer.getBestCategory(outcomes);

        //        System.out.println(category);
        if (category.equalsIgnoreCase("0")) {
            return 0;
        } else if(category.equalsIgnoreCase("1")) {
            return 1;
        }
        else if(category.equalsIgnoreCase("2")) {
            return 2;
        }
        else if(category.equalsIgnoreCase("3")) {
            return 3;
        }
        else{
            return 4;
        }
    }

}
