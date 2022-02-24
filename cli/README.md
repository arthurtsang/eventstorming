# Event Storming

This tools convert Event Markdown Language (EML) defined at [WebEventStorming](http://webeventstorming.com) and generate the graph using PlantUML instead.

WebEventStorming is the only web site that provide [PlantUML](http://plantuml.com) like service for generating Event Storming diagram.  However, it has very limited support in types, no static URL, comments are ignored and the generated image is just aesthetic inferior to that of PlantUML.

PlantUML itself doesn't support Event Storming diagram.  There is a lot of boiler plate code and hidden arrows needed to generate the diagram, and that also make changing an existing diagram a very complicated task.

Thus, combining the two, we've simple syntax, feature rich (more work need to be done) and aesthetically good looking diagrams.

## Usage

java -jar tools-eventstorming-<version>.jar -f <EML filename> -p <puml filename> -o <png filename>

-p is optional.

