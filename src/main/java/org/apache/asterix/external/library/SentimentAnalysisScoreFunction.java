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
package org.apache.asterix.external.library;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import org.apache.asterix.external.api.IExternalScalarFunction;
import org.apache.asterix.external.api.IFunctionHelper;

import org.apache.asterix.external.library.java.JObjects.JString;
import org.apache.asterix.external.library.java.JObjects.JInt;
import org.apache.asterix.external.library.java.JTypeTag;

import java.io.InputStream;


public class SentimentAnalysisScoreFunction implements IExternalScalarFunction {


    private static DoccatModel m;

    @Override
    public void evaluate(IFunctionHelper functionHelper) throws Exception {
        JString text = ((JString) functionHelper.getArgument(0));

        JInt result = (JInt) functionHelper.getObject(JTypeTag.INT);


        //Getting sentiment score
        int score = getSentiment(text.getValue(), m);


        result.setValue(score);
        functionHelper.setResult(result);
    }


    @Override
    public void deinitialize() {
        System.out.println("De-Initialized");
    }


    @Override
    public void initialize(IFunctionHelper functionHelper) throws Exception {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("en-doccat.bin");
        m = new DoccatModel(in);
    }


    public static int getSentiment(String tweet, DoccatModel model) {
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
        double[] outcomes = myCategorizer.categorize(tweet);
        String category = myCategorizer.getBestCategory(outcomes);


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
