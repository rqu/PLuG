PLuG
====

This is a trimmed down version of [DiSL]. The key difference is that while DiSL does the instrumentations at run time, PLuG takes `.jar` file containing the application and produces an instrumented `.jar` file which can then be run using any JVM (without any native agents).

[DiSL]: <https://gitlab.ow2.org/disl/disl>


Building
--------

1. Clone this repository using `git clone https://github.com/rqu/PLuG.git`
2. Change into the directory: `cd PLuG`
3. Run `ant` without parameters to build it.


Running
-------

First get an application to be instrumented and the instrumentation itself. You can start with some [examples] from the DiSL repository.
The standard way to run PLuG is `plug.sh`. Run it without parameters to get help.
When the instrumentation is done, you can start your application by `java -jar whatever_the_output_was.jar`.

[examples]: <https://gitlab.ow2.org/disl/disl/tree/master/examples>


Further information
-------------------

For the API documentation refer to [introduction to DiSL] (TEX), as this tool is just a fork of DiSL. All credit goes to DiSL developers.

[introduction to DiSL]: <https://gitlab.ow2.org/disl/disl/blob/master/doc/intro/dislintro.tex>
