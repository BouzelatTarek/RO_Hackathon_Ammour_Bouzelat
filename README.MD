# Operation Research challenge - DS4SC 
## Subject
During classes, you have studied two typical Operation Research problems, the Knapsack Problem (KP) and the Travelling Salesman Problem (TSP).
We will now study a problem that takes aspects from both KP and TSP. This problem stems from the need to automate Exploratory Data Analysis (EDA), 
typically as BDMA students given an unknown database you would spend several hours running queries to extract interesting information for a business case.
Our goal is to automate part of this process by showing you a sequence of interesting queries. 
This can be seen as an optimization problem. For a set of possible queries on a database we create a complete graph, on each node the cost to run the query and its interestingness. On each edge the distance between the two queries.
We then look for a path in the graph (A sequence of nodes) with the maximum interestingness given a cost and distance limit.
This problem is called te Travelling Analyst Problem (TAP) and is closely related to transport problems such as the Orienteering Problem.
A detailed explanation is available [here](https://hal.archives-ouvertes.fr/hal-03217380/file/tap_roadef_final.pdf).

The baseline strategy you should beat is simply sorting queries by interestingness and building the sequence until one constraint is violated.
## Expected work
This is a 2-person 'hackathon' that will encompass the lab 'day' dedicated to OR. Your goal is to propose one or more heuristic(s) to solve the TAP problem and implement them following the TAPSolver Interface.
You will document your work and reasoning behind your algorithm(s) in a lab report. 
### Grading
Each 2-person team will be graded on a 2 or 3 pages (hard limit) report, the code quality, and the performance of the heuristics (runtime, objective).
Your heuristics will be tested on other instances as well as the 5 provided in this repository. 
Heuristics may be written in any programming language, but you must include a method designed to output to the following csv format (Team token / Id of the instance / Your solution as a sequence of queries):
```
student_id;instance_id;1,0,2,3
```
#### Source code grading
Note that the grading of your sources **will include** the easy of deployment. 
For example using python libraries without providing a requirements.txt will be penalized. 
If you use a compiled language please include a statically linked binary compatible with Windows 11 or include a makefile working on any Linux (arm64) distribution.

### Evaluating your solution
The optimal and baseline objective values for the following instances are given: `f1_tap_3_400.dat; f1_tap_9_400.dat; f4_tap_0_20.dat; f4_tap_1_400.dat; f4_tap_4_400.dat; tap_10_100.dat; tap_11_250.dat; tap_13_150.dat; tap_14_400.dat; tap_15_60.dat`.
To evaluate your algorithm (or the baseline) you can compute the relative gap to the optimal solution's objective : `100 * ( opt_obj - your_obj ) / opt_obj`
To do so you may use `sol_interest` (Python) or the `Ojectives` class (Java) or write your own code.

## Exemple (Naive Solution)
Create a class `TestNaif` that implements the `TAPSolver` interface. 
As you can see in the interface we need to implement a solve method with the following signature `public List<Integer> solve(Instance ist)`. 
To construct the solution we will simply chain the queries in the order they appear in the `Instance` until we violate one epsilon constraint.

To do so we can use the `Objectives` object:
```java
        List<Integer> demo = new ArrayList<>();
        Objectives obj = new Objectives(ist);
        
        int q_idx = 0;
        
        while (obj.distance(demo) < ist.getMaxDistance() && obj.time(demo) < ist.getTimeBudget()){
            demo.add(q_idx++);
        }
        return demo.subList(0, demo.size() - 1);
```

## Using your own PC
You should have Maven installed and an updated JDK.

Download Project
```shell
git clone https://github.com/AlexChanson/DS4SC-OperationResearch
```
Move to project and test build
```shell
cd .\DS4SC-OperationResearch\
mvn package
```

### Run your code 
```shell
java -cp target/TP-RO-M1-BDMA-1.0-SNAPSHOT.jar com.alexscode.teaching.Main
```

## Using computer lab PCs
### Ouvrir powershell
Set proxy for git
```
git config --global https.proxy http://proxy:3128
git config --global http.proxy http://proxy:3128
```

Download Project
```shell
git clone https://github.com/AlexChanson/DS4SC-OperationResearch
```

Setup maven (Only use last line if you have already downloaded)
```
wget https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip -UseBasicParsing -O maven.zip
Expand-Archive .\maven.zip .
$env:Path += 'U:\apache-maven-3.8.8\bin'
```

Set Proxy for maven
```shell
mkdir $Env:userprofile\.m2
cp .\RechercheOperationnelle-M1-BDMA\config\settings.xml $Env:userprofile\.m2\settings.xml
```


Check if it's working with `mvn --version`

Setup Java Environment
```
$Env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_341\"
```

Move to project and test build
```shell
cd .\DS4SC-OperationResearch\
mvn package
```

### Run your code
```shell
java -cp target/TP-RO-M1-BDMA-1.0-SNAPSHOT.jar com.alexscode.teaching.Main
```

## .dat format
The instances are provided in text format as shown bellow, note that in this format queries are identified implicitly by their position :
```
3
0.3 0.5 0.7
12 14 23
0 1 3
1 0 4
3 4 0
```
The first line is the number of queries, the second their interestingness, the third the time required to run them. Finally the distance matrix occupies the remaining lines.
For example q_0 has an interestingness of 0.3 a cost (time) of 12 and it's distance to q_2 is 3.