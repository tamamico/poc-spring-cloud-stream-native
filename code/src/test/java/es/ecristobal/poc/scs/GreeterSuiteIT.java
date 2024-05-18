package es.ecristobal.poc.scs;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({MenGreeter.class, WomenGreeter.class})
class GreeterSuiteIT {
}
