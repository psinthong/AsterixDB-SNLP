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
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.asterix.external.api.IExternalScalarFunction;
import org.apache.asterix.external.api.IFunctionHelper;
import org.apache.asterix.external.library.java.JBuiltinType;
import org.apache.asterix.external.library.java.base.JOrderedList;
import org.apache.asterix.external.library.java.base.JRecord;
import org.apache.asterix.external.library.java.base.JString;
import org.apache.asterix.external.library.java.JTypeTag;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class PersonRecognitionFunction implements IExternalScalarFunction {

    private static StanfordCoreNLP pipeline;

    @Override
    public void deinitialize() {
        System.out.println("De-Initialized");
    }

    @Override
    public void evaluate(IFunctionHelper functionHelper) throws Exception {

        JRecord inputRecord = (JRecord) functionHelper.getArgument(0);
        JString text = (JString) inputRecord.getValueByName("text");
        JRecord record = (JRecord) functionHelper.getResultObject();

        //NER
        JOrderedList nameList = new JOrderedList(JBuiltinType.JSTRING);
        nameList.add(functionHelper.getObject(JTypeTag.STRING));
        Set<String> names = ner(text.getValue());


        for(String name : names)
        {

            JString nameJ = (JString) functionHelper.getObject(JTypeTag.STRING);
            nameJ.reset();
            nameJ.setValue(name);
            nameList.add(nameJ);
        }

        record.setField("id", inputRecord.getValueByName("id"));
        record.setField("text", text);
        record.setField("entities", nameList);

        functionHelper.setResult(record);
    }

    @Override
    public void initialize(IFunctionHelper functionHelper) throws Exception {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        pipeline = new StanfordCoreNLP(props);
    }


    public static Set<String> ner(String text)
    {

        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        Set<String> locations = new HashSet<>();

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);

                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if(ne.equals("PERSON"))
                {
                    locations.add(word);
                }
            }
        }

        return locations;

    }

}
