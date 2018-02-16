
# Using Stanford CoreNLP & OpenNLP UDFs 


This document describe the process for using Stanford CoreNLP and OpenNLP packages with user-defined functions(UDF) in AsterixDB. We assume you have followed the [installation instructions](http://asterixdb.apache.org/docs/0.9.0/install.html) to set up a running AsterixDB instance.

## How To Use
* Clone this repo onto your local machine.
* Build this project(use `mvn install` or `mvn package`).
* The UDF package will be under `target/` directory. This is the external library that will be loaded during [installation process](#install)
* [Download necessary library files](#library)
* [Install external libraries with Ansible](#install_ansible)
* [Install external libraries with Managix](#install_managix)
* [Apply UDFs](#apply)

## <a name="library">Download library files</a>
- Download Stanford CoreNLP jar files from [Stanford website](https://stanfordnlp.github.io/CoreNLP/download.html)
or a [private repository](https://drive.google.com/open?id=0B8f-3gEi4pmhcUQzNzFQSUxpTEk) Note: you will only need three jar files which are `stanford-corenlp.jar`, `stanford-corenlp-model.jar` and `ejml.jar`
- Download OpenNLP jar files : [tools](https://mvnrepository.com/artifact/org.apache.opennlp/opennlp/1.7.0)
and [models](https://mvnrepository.com/artifact/org.apache.opennlp/opennlp-maxent/3.0.3) Note: you will only need two jar files which are `opennlp-tools-1.7.0.jar` and `opennlp-maxent.jar` 
- Drop these jar files into asterix-server zip folder (ie. `asterix-server-0.9.0-binary-assembly.zip`). You will need to unzip this asterix-server zip file and drop all jars into `repo`/ folder then zip it back.
    - zip -rg `asterix-server-0.9.0-binary-assembly.zip` `asterix-server-0.9.0-binary-assembly`

## <a name="install_ansible"> Install external libraries with Ansible</a>
AsterixDB provides Ansible as one of its installation option. With Ansible, we can
easily deploy UDF and its dependencies to all nodes.

* Follow the instruction in [AsterixDB documentation](https://ci.apache.org/projects/asterixdb/ansible.html) and
deploy AsterixDB to the cluster. If you have any dependencies that is required for your UDF, copy them into `repo`
directory before deploy. In this example, you need to copy `stanford-corenlp.jar`, `stanford-corenlp-model.jar`
and `ejml.jar` into this directory.
* Make sure your instance is stopped before install UDF.
* Find `udf.sh` under `opt/ansible/bin` and deploy your UDF package to all nodes using following command:

        ./udf.sh -m i -d DATAVERSE_NAME -l LIBRARY_NAME -p UDF_PACKAGE_PATH

  If the target dataverse doesn't exist, it will be created automatically with the UDF installation.
* Start your instance and have fun with your UDF.

## <a name="install_managix">Install External Libraries with Managix</a>

Managix is another installtion option. Setup an running instance with [AsterixDB documentation](https://ci.apache.org/projects/asterixdb/install.html).
Let us refer to your AsterixDB instance by the name "my_asterix".

**Step 1**: Stop the AsterixDB instance if it is in the ACTIVE state.

        $ managix stop  -n my_asterix

**Step 2**: Install the library using Managix install command. Just to illustrate, we use the help command to look up the syntax

        $ managix help  -cmd install
        Installs a library to an asterix instance.
        Options
        n  Name of Asterix Instance
        d  Name of the dataverse under which the library will be installed
        l  Name of the library
        p  Path to library zip bundle

Above is a sample output and explains the usage and the required parameters. Each library has a name and is installed under a dataverse. Recall that we had created a dataverse by the name - "feeds" prior to creating our datatypes and dataset. We shall name our library - "snlp", but ofcourse, you may choose another name.

You may place the pre-packaged library(a zip bundle generated using this codebase) at a convenient location on your disk. To install the library, use the Managix install command. An example is shown below.

    $ managix install -n my_asterix -d feeds -l snlp -p <put the absolute path of the library zip bundle here> 

You should see the following message:

    INFO: Installed library snlp

We shall next start our AsterixDB instance using the start command as shown below.

    $ managix start -n my_asterix


You may now use the AsterixDB library in AQL statements and queries. To look at the installed artifacts, you may execute the following query at the AsterixDB web-console.

    SELECT VALUE f FROM Metadata.`Function` f;

    SELECT VALUE l dataset FROM Metadata.`Library` l;


## <a name="apply">Applying UDFs</a>

### Creating Input Data Types
The following query creates a dataverse, that acts as a namespace for all datatypes that we also create there after. We assume that these UDFs will be applied to Twitter data for which we expect a specific schema.

    drop feeds if exists;
    create dataverse feeds;
    use feeds;


    create type Tweet as open {
        id: int64,
        text : string
    };

### Creating Output Data Types

    use feeds;
    create type NameEntityType if not exists as closed{
        id: int64,
        text: string,
        entities: [string]
    };
    create type TweetSentimentType if not exists as closed{
        id: int64,
        text: string,
        score: int32,
        sentiment: string
    };

### Sample Query for Stanford CoreNLP sentiment

    snlp#getSNLPSentiment({"id":1, "text":"Today is Friday"})

    snlp#getSNLPSentimentScore("Today is Friday")
    
### Sample Query for OpenNLP sentiment

    snlp#getONLPSentiment({"id":1, "text":"Today is Friday"})
    
    snlp#getONLPSentimentScore("Today is Friday")
    
## Function Usage

### Syntax for Sentiment Analysis

##### Stanford CoreNLP & OpenNLP

        snlp#getSNLPSentiment($item)
        
        snlp#getONLPSentiment($item)

- Runs analysis on a given text and gives back a score in range of 0-4
- Argument:
    + item: a data record of type Tweet with an attribute `text`
- Return Value:
    + a record of type SentimentType.
- Expected Result:

        {   
            "id": 1, 
            "text": "Today is Friday", 
            "score": 2, 
            "sentiment": "Neutral" 
        }
        
##### Only get Sentiment score
 
     snlp#getSNLPSentimentScore($item)

     snlp#getONLPSentimentScore($item)

- Runs analysis on a given text and gives back a score in range of 0-4
- Argument:
    + item: string
- Return Value:
    + int32



### Syntax for Date Recognition

        snlp#getDate($item)

- Runs analysis on a given text and extracts our Date entities
- Argument:
    + item: a data record of type Tweet with an attribute `text`
- Return Value:
    + a record of type NameEntityType.
- Expected Result:

        {   
            "id": 1, 
            "text": "Yesterday was Thursday",
            "entities": [ "Thursday", "Yesterday" ] 
        }

### Syntax for Location Recognition

        snlp#getLocation($item)

- Runs analysis on a given text and extracts out Person entities
- Argument:
    + item: a data record of type Tweet with an attribute `text`
- Return Value:
    + a record of type NameEntityType.
- Expected Result:

        { 
            "id": 1, 
            "text": "I fly from NYC to London",
            "entities": [ "NYC", "London" ] 
        }

### Syntax for Person Recognition

        snlp#getName($item)

- Runs analysis on a given text and extracts our Location entities
- Argument:
    + item: a data record of type Tweet with an attribute `text`
- Return Value:
    + a record of type NameEntityType.
- Expected Result:

        { 
            "id": 1, 
            "text": "Obama flips Bush admin's policies",
            "entities": [ "Obama", "Bush" ] 
        }
